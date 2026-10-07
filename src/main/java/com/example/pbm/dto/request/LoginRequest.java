package com.example.pbm.dto.request;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
public record LoginRequest(@NotBlank String username, @NotBlank String password, UUID tenantId) {}
