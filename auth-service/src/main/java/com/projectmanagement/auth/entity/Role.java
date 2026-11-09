package com.projectmanagement.auth.entity;

import com.projectmanagement.auth.entity.enums.RoleCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 32)
    private RoleCode code;

    @Column(nullable = false, length = 100)
    private String name;

    @OneToMany(mappedBy = "role")
    private Set<UserRole> userRoles = new HashSet<>();
}