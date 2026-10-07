package com.example.pbm.repository;
import com.example.pbm.entity.Member;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MemberRepository extends JpaRepository<Member, UUID> {
    List<Member> findByTenantId(UUID tenantId);
    Optional<Member> findByIdAndTenantId(UUID id, UUID tenantId);
}
