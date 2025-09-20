package com.vote.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.vote.entity.Voter;

public interface VoterRepository extends JpaRepository<Voter, Long> {

    Optional<Voter> findById(Long id);
    
    long countByHasVoted(boolean hasVoted);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO voter (id, has_voted) " +
                   "SELECT id, false FROM auth WHERE role = 'VOTER' AND id NOT IN (SELECT id FROM voter)", nativeQuery = true)
    void insertVoterIdsFromAuth();
    
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO voter (id, has_voted) VALUES (:id, false)", nativeQuery = true)
    void insertSingleVoter(@Param("id") Long id);
    
    @Modifying
    @Transactional
    @Query("UPDATE Voter v SET v.hasVoted = :hasVoted WHERE v.id = :voterId")
    void updateVotingStatus(@Param("voterId") Long voterId, @Param("hasVoted") Boolean hasVoted);
}