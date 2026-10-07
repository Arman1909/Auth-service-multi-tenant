package com.example.pbm.service;
import com.example.pbm.entity.*;
import com.example.pbm.repository.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Seeds superadmin + demo tenants A/B (all passwords: "password"). */
@Component @RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final UserRepository users; private final TenantRepository tenants;
    private final MemberRepository members; private final ClaimRepository claims;
    private final PasswordEncoder encoder;
    @Value("${app.seed-demo:true}") private boolean demo;

    @Override public void run(String... args) {
        if (users.existsByUsername("superadmin")) return;
        user("superadmin", Role.SUPER_ADMIN, DeRole.PLATFORM);
        if (!demo) return;
        Tenant a = tenants.save(Tenant.of("Tenant A")), b = tenants.save(Tenant.of("Tenant B"));
        user("tenantadmin1", Role.TENANT_ADMIN, DeRole.OWN_TENANT, a.getId(), b.getId());
        user("usera1", Role.TENANT_USER, DeRole.OWN_TENANT, a.getId());
        user("userb1", Role.TENANT_USER, DeRole.OWN_TENANT, b.getId());
        for (Tenant t : new Tenant[]{a, b}) {
            Member m = members.save(new Member(UUID.randomUUID(), t.getId(), "Member of " + t.getName()));
            members.save(new Member(UUID.randomUUID(), t.getId(), "Second member of " + t.getName()));
            claims.save(new Claim(UUID.randomUUID(), t.getId(), m.getId(), new BigDecimal("125.50"), "PAID"));
        }
    }
    private void user(String name, Role r, DeRole de, UUID... tenantIds) {
        User u = User.of(name, encoder.encode("password"), r, de);
        for (UUID t : tenantIds) u.getTenantIds().add(t);
        users.save(u);
    }
}
