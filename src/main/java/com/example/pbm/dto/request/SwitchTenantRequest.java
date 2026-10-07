package com.example.pbm.dto.request;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
public record SwitchTenantRequest(@NotNull UUID tenantId) {}
