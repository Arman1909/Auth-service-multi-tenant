package com.example.pbm.dto.response;
import com.example.pbm.entity.Member;
import java.util.UUID;
public record MemberResponse(UUID id, UUID tenantId, String memberName) {
    public static MemberResponse from(Member m) { return new MemberResponse(m.getId(), m.getTenantId(), m.getMemberName()); }
}
