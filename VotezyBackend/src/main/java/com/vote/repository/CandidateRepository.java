package com.vote.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.vote.entity.Candidate;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    Optional<Candidate> findById(Long id);
    
    // Check if party name already exists
    boolean existsByPartyName(String partyName);
    
    // Check if symbol already exists
    boolean existsBySymbol(String symbol);
    
    // Check if party name exists for different candidate
    boolean existsByPartyNameAndIdNot(String partyName, Long id);
    
    // Check if symbol exists for different candidate
    boolean existsBySymbolAndIdNot(String symbol, Long id);
    
    
    // Count candidates by status
    long countByStatus(String status);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO candidate (id, party_name, symbol, vote_count, status) " +
                   "SELECT id, null, null, 0, 'Not Completed' FROM auth WHERE role = 'CANDIDATE' AND id NOT IN (SELECT id FROM candidate)", nativeQuery = true)
    void insertCandidateIdsFromAuth();
    
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO candidate (id, party_name, symbol, vote_count, status) VALUES (:id, null, null, 0, 'Not Completed')", nativeQuery = true)
    void insertSingleCandidate(@Param("id") Long id);
    
    @Modifying
    @Transactional
    @Query("UPDATE Candidate c SET c.voteCount = :voteCount WHERE c.id = :candidateId")
    void updateVoteCount(@Param("candidateId") Long candidateId, @Param("voteCount") Long voteCount);
    
    
}