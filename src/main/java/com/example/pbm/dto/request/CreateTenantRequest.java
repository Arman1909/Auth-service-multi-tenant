package com.example.pbm.dto.request;
import jakarta.validation.constraints.*;
public record CreateTenantRequest(@NotBlank @Size(max = 150) String name) {}
