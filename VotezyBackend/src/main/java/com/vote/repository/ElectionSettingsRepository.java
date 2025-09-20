package com.vote.repository;

import com.vote.entity.ElectionSettings;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ElectionSettingsRepository extends JpaRepository<ElectionSettings, Long> {
	@Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE election_settings", nativeQuery = true)
    void truncateTable();

	
	@Query("SELECT COUNT(e) > 0 FROM ElectionSettings e WHERE e.status = 'active' AND e.resultStatus = 'not_declared'")
	boolean isElectionRunning();

}