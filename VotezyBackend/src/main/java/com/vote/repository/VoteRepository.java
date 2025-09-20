package com.vote.repository;


import com.vote.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    
    // Check if voter has already voted
    boolean existsByVoterId(Long voterId);
    
    // Get vote by voter ID
    Optional<Vote> findByVoterId(Long voterId);
    
    // Count votes for a specific candidate
    long countByCandidateId(Long candidateId);
    
    // Get all votes for a specific candidate
    List<Vote> findByCandidateId(Long candidateId);
    
    // Get voting results grouped by candidate
    @Query("SELECT v.candidateId, COUNT(v) FROM Vote v GROUP BY v.candidateId")
    List<Object[]> getVotingResults();
    
    // Total votes cast
    @Query("SELECT COUNT(v) FROM Vote v")
    long getTotalVotesCast();
    
   
    
    

}