package com.projectmanagement.auth.repository;

import com.projectmanagement.auth.entity.UserRole;
import com.projectmanagement.auth.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    List<UserRole> findAllByIdUserId(UUID userId);

    boolean existsByIdUserIdAndIdRoleId(UUID userId, UUID roleId);

    void deleteAllByIdUserId(UUID userId);
}