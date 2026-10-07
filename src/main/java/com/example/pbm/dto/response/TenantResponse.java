package com.example.pbm.dto.response;
import com.example.pbm.entity.Tenant;
import java.util.UUID;
public record TenantResponse(UUID id, String name, String status) {
    public static TenantResponse from(Tenant t) { return new TenantResponse(t.getId(), t.getName(), t.getStatus()); }
}
