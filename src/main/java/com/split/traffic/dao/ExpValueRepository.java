package com.split.traffic.dao;

import com.split.traffic.dao.entities.ExpValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface ExpValueRepository extends JpaRepository<ExpValueEntity, Long> {
    @Query(value = "SELECT id, expId, expValue, trafficPercent from EXP_VALUE where expId = :expId", nativeQuery = true)
    List<ExpValueEntity> findByExpId(@Param("expId") Long expId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE EXP_VALUE set trafficPercent = :trafficPercent where id = :id limit 1", nativeQuery = true)
    void updateTraffic(@Param("id") Long id, @Param("trafficPercent") Integer trafficPercent);
}
