package com.example.pbm;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.example.pbm.entity.*;
import com.example.pbm.repository.*;
import com.example.pbm.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;

@SpringBootTest @AutoConfigureMockMvc
class SecurityBoundaryTest {
    @Autowired MockMvc mvc; @Autowired ObjectMapper om;
    @Autowired TenantRepository tenants; @Autowired UserRepository users;
    @Autowired MemberRepository members; @Autowired ClaimRepository claims; @Autowired JwtService jwtService;
    UUID A, B;
    @BeforeEach void ids() { A = tenants.findByName("Tenant A").get().getId(); B = tenants.findByName("Tenant B").get().getId(); }

    String login(String user, UUID tenant) throws Exception {
        Map<String, Object> body = new HashMap<>(Map.of("username", user, "password", "password"));
        if (tenant != null) body.put("tenantId", tenant.toString());
        String res = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(body)))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return om.readTree(res).get("accessToken").asText();
    }
    ResultActions get(String token, String url) throws Exception { return mvc.perform(get(url).header("Authorization", "Bearer " + token)); }
    ResultActions post(String token, String url, Object body) throws Exception {
        return mvc.perform(post(url).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(body)));
    }
    String sfx() { return UUID.randomUUID().toString().substring(0, 8); }

    @Test void t01_superAdminLogin() throws Exception { assertFalse(login("superadmin", null).isBlank()); }

    @Test void t02_t03_createTenantAndAssignAdmin() throws Exception {
        String s = login("superadmin", null);
        String res = post(s, "/api/v1/tenants", Map.of("name", "T-" + sfx())).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String tid = om.readTree(res).get("id").asText();
        get(s, "/api/v1/tenants").andExpect(status().isOk());
        post(s, "/api/v1/tenants/" + tid + "/admins", Map.of("username", "adm-" + sfx(), "password", "password123")).andExpect(status().isOk());
    }

    @Test void t04_adminBelongsToMultipleTenants() {
        assertEquals(Set.of(A, B), users.findByUsername("tenantadmin1").get().getTenantIds());
    }

    @Test void t05_adminSelectsAndSwitchesAssignedTenant() throws Exception {
        String t = login("tenantadmin1", A);
        post(t, "/api/v1/auth/switch-tenant", Map.of("tenantId", B.toString())).andExpect(status().isOk());
    }

    @Test void t06_adminCannotSelectUnassignedTenant() throws Exception {
        String s = login("superadmin", null);
        String res = post(s, "/api/v1/tenants", Map.of("name", "T-" + sfx())).andReturn().getResponse().getContentAsString();
        UUID other = UUID.fromString(om.readTree(res).get("id").asText());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(Map.of("username", "tenantadmin1", "password", "password", "tenantId", other.toString())))).andExpect(status().isForbidden());
        post(login("tenantadmin1", A), "/api/v1/auth/switch-tenant", Map.of("tenantId", other.toString())).andExpect(status().isForbidden());
    }

    @Test void t07_tenantUserHasExactlyOneTenant() throws Exception {
        assertEquals(1, users.findByUsername("usera1").get().getTenantIds().size());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(Map.of("username", "usera1", "password", "password", "tenantId", B.toString())))).andExpect(status().isForbidden());
        post(login("superadmin", null), "/api/v1/tenants/" + B + "/admins", Map.of("username", "usera1")).andExpect(status().isConflict());
    }

    @Test void t08_t19_adminCreatesUserInActiveTenant_ignoringBodyTenantId() throws Exception {
        String ta = login("tenantadmin1", A); String name = "u-" + sfx();
        post(ta, "/api/v1/users", Map.of("username", name, "password", "password123", "tenantId", B.toString())).andExpect(status().isCreated());
        assertEquals(Set.of(A), users.findByUsername(name).get().getTenantIds());
        get(ta, "/api/v1/users").andExpect(jsonPath("$[*].username", hasItem(name)));
        get(login("tenantadmin1", B), "/api/v1/users").andExpect(jsonPath("$[*].username", not(hasItem(name))));
    }

    @Test void t09_t10_t11_userIsolation() throws Exception {
        UUID userB = users.findByUsername("userb1").get().getId();
        for (String token : List.of(login("tenantadmin1", A), login("usera1", null))) {
            get(token, "/api/v1/users").andExpect(jsonPath("$[*].username", hasItem("usera1"))).andExpect(jsonPath("$[*].username", not(hasItem("userb1"))));
            get(token, "/api/v1/users?tenantId=" + B).andExpect(jsonPath("$[*].username", not(hasItem("userb1"))));
            get(token, "/api/v1/users/" + userB).andExpect(status().isNotFound());
        }
    }

    @Test void t12_to_t15_membersAndClaimsIsolation() throws Exception {
        UUID memB = members.findByTenantId(B).get(0).getId(), memA = members.findByTenantId(A).get(0).getId();
        UUID claimB = claims.findByTenantId(B).get(0).getId();
        for (String token : List.of(login("tenantadmin1", A), login("usera1", null))) {
            get(token, "/api/v1/members").andExpect(status().isOk()).andExpect(jsonPath("$[*].tenantId", everyItem(is(A.toString()))));
            get(token, "/api/v1/members/" + memA).andExpect(status().isOk());
            get(token, "/api/v1/members/" + memB).andExpect(status().isNotFound());
            get(token, "/api/v1/claims").andExpect(jsonPath("$[*].tenantId", everyItem(is(A.toString()))));
            get(token, "/api/v1/claims/" + claimB).andExpect(status().isNotFound());
        }
    }

    @Test void t16_invalidOrMissingJwt() throws Exception {
        mvc.perform(get("/api/v1/members")).andExpect(status().isUnauthorized());
        get("garbage.token.value", "/api/v1/members").andExpect(status().isUnauthorized());
        String t = login("usera1", null);
        get(t.substring(0, t.length() - 4) + "AAAA", "/api/v1/members").andExpect(status().isUnauthorized());
    }

    @Test void t17_missingFeIs403() throws Exception {
        post(login("usera1", null), "/api/v1/users", Map.of("username", "x" + sfx(), "password", "password123")).andExpect(status().isForbidden());
        get(login("superadmin", null), "/api/v1/members").andExpect(status().isForbidden()); // platform admin has no tenant-data FE
        get(login("tenantadmin1", A), "/api/v1/tenants").andExpect(status().isForbidden());
    }

    @Test void t18_invalidDeIs403() throws Exception {
        User u = User.of("forged", "x", Role.TENANT_ADMIN, DeRole.PLATFORM); // FE ok, DE not
        get(jwtService.issue(u, null), "/api/v1/members").andExpect(status().isForbidden());
        User noTenant = User.of("forged2", "x", Role.TENANT_ADMIN, DeRole.OWN_TENANT); // DE ok, no active tenant
        get(jwtService.issue(noTenant, null), "/api/v1/members").andExpect(status().isForbidden());
    }
}
