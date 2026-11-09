package com.projectmanagement.auth.repository;

import com.projectmanagement.auth.entity.Role;
import com.projectmanagement.auth.entity.enums.RoleCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByCode(RoleCode code);
}