package com.example.pbm.repository;
import com.example.pbm.entity.Tenant;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    boolean existsByName(String name);
    Optional<Tenant> findByName(String name);
}
