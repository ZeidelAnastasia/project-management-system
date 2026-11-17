package com.projectmanagement.intelligence.repository;

import com.projectmanagement.intelligence.entity.RiskFactorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RiskFactorRepository extends JpaRepository<RiskFactorEntity, UUID> {

    List<RiskFactorEntity> findAllByAssessmentId(UUID assessmentId);

    void deleteAllByAssessmentId(UUID assessmentId);
}