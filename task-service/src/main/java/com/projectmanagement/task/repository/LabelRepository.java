
package com.projectmanagement.task.repository;

import com.projectmanagement.task.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LabelRepository extends JpaRepository<Label, UUID> {

    Optional<Label> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
