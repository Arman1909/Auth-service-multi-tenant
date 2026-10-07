package com.example.pbm.entity;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;
@Entity @Table(name = "members") @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Member {
    @Id private UUID id;
    @Column(nullable = false) private UUID tenantId;
    @Column(nullable = false) private String memberName;
}
