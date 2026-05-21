package com.example.oms.repository;

import com.example.oms.entity.IntegrationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IntegrationLogRepository extends JpaRepository<IntegrationLog, Long> {
    List<IntegrationLog> findByScenarioIdOrderByTimestampAsc(Integer scenarioId);
    List<IntegrationLog> findTop50ByOrderByTimestampDesc();
}
