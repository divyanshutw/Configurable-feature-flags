package com.split.traffic.dao;

import com.split.traffic.dao.entities.ExpNameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpNameRepository extends JpaRepository<ExpNameEntity, Long> {
    @Query(value = "Select expId, expName, defaultValue, isActive, startTime, endTime from EXP_NAME where expName = :expName limit 1", nativeQuery = true)
    ExpNameEntity findByExpName(@Param("expName") String expName);
}
