package com.example.pbm.controller;
import com.example.pbm.dto.request.CreateUserRequest;
import com.example.pbm.dto.response.UserResponse;
import com.example.pbm.service.UserService;
import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/users") @RequiredArgsConstructor
public class UserController {
    private final UserService svc;
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAuthority('USER_CREATE')")
    public UserResponse create(@Valid @RequestBody CreateUserRequest r) { return svc.create(r); }
    @GetMapping @PreAuthorize("hasAuthority('USER_READ')")
    public List<UserResponse> list() { return svc.list(); }
    @GetMapping("/{id}") @PreAuthorize("hasAuthority('USER_READ')")
    public UserResponse get(@PathVariable UUID id) { return svc.get(id); }
}
