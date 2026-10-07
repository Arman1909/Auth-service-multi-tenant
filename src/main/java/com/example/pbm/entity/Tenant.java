package com.example.pbm.entity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
@Entity @Table(name = "tenants") @Getter @Setter @NoArgsConstructor
public class Tenant {
    @Id private UUID id;
    @Column(nullable = false, unique = true, length = 150) private String name;
    @Column(nullable = false, length = 20) private String status = "ACTIVE";
    @Column(nullable = false) private Instant createdAt = Instant.now();
    public static Tenant of(String name) { Tenant t = new Tenant(); t.id = UUID.randomUUID(); t.name = name; return t; }
}
