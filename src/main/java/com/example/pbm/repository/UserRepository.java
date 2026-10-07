package com.example.pbm.repository;
import com.example.pbm.entity.User;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    @Query("select u from User u join u.tenantIds t where t = :tid")
    List<User> findByTenant(@Param("tid") UUID tenantId);
    @Query("select u from User u join u.tenantIds t where t = :tid and u.id = :id")
    Optional<User> findByIdAndTenant(@Param("id") UUID id, @Param("tid") UUID tenantId);
}
