package com.example.pbm.service;
import com.example.pbm.dto.request.CreateUserRequest;
import com.example.pbm.dto.response.UserResponse;
import com.example.pbm.entity.*;
import com.example.pbm.repository.UserRepository;
import com.example.pbm.tenant.TenantContext;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service @RequiredArgsConstructor
public class UserService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final TenantContext ctx;

    public UserResponse create(CreateUserRequest r) {
        UUID tenant = ctx.requireActiveTenant();
        if (users.existsByUsername(r.username())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Username exists");
        User u = User.of(r.username(), encoder.encode(r.password()), Role.TENANT_USER, DeRole.OWN_TENANT);
        u.getTenantIds().add(tenant); // exactly one tenant for a TENANT_USER
        return UserResponse.from(users.save(u));
    }
    public List<UserResponse> list() { return users.findByTenant(ctx.requireActiveTenant()).stream().map(UserResponse::from).toList(); }
    public UserResponse get(UUID id) {
        return users.findByIdAndTenant(id, ctx.requireActiveTenant()).map(UserResponse::from)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
