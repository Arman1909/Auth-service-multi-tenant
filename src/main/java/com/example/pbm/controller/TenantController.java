package com.example.pbm.controller;
import com.example.pbm.dto.request.*;
import com.example.pbm.dto.response.*;
import com.example.pbm.service.TenantService;
import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/tenants") @RequiredArgsConstructor
public class TenantController {
    private final TenantService svc;
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAuthority('TENANT_CREATE')")
    public TenantResponse create(@Valid @RequestBody CreateTenantRequest r) { return svc.create(r); }
    @GetMapping @PreAuthorize("hasAuthority('TENANT_READ')")
    public List<TenantResponse> list() { return svc.list(); }
    @PostMapping("/{tenantId}/admins") @PreAuthorize("hasAuthority('TENANT_ASSIGN_ADMIN')")
    public UserResponse assign(@PathVariable UUID tenantId, @Valid @RequestBody AssignTenantAdminRequest r) { return svc.assignAdmin(tenantId, r); }
}
