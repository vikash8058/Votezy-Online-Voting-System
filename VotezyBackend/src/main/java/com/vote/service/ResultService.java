package com.vote.service;

import com.vote.dto.ElectionResultDTO;
import com.vote.dto.WinnerDTO;
import com.vote.entity.Auth;
import com.vote.entity.Candidate;
import com.vote.repository.CandidateRepository;
import com.vote.repository.UserRepository;
import com.vote.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ResultService {

    @Autowired
    private CandidateRepository candidateRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ElectionSettingsService electionSettingsService;

    public List<ElectionResultDTO> getElectionResults() {
        // Check if results are declared
        if (!electionSettingsService.areResultsDeclared()) {
            throw new IllegalStateException("Election results have not been declared yet");
        }
        
        // Get all completed candidates with their vote counts
        List<Candidate> candidates = candidateRepository.findAll().stream()
            .filter(candidate -> "Completed".equals(candidate.getStatus()))
            .sorted((c1, c2) -> Long.compare(c2.getVoteCount(), c1.getVoteCount())) // Descending order
            .collect(Collectors.toList());
        
        if (candidates.isEmpty()) {
            throw new ResourceNotFoundException("No completed candidates found");
        }
        
        // Convert to DTOs with ranking
        return IntStream.range(0, candidates.size())
            .mapToObj(i -> {
                Candidate candidate = candidates.get(i);
                Auth auth = userRepository.findById(candidate.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Candidate auth not found"));
                
                return new ElectionResultDTO(
                    candidate.getId(),
                    auth.getFullName(),
                    candidate.getPartyName(),
                    candidate.getSymbol(),
                    candidate.getVoteCount(),
                    i + 1 // Rank (1-based)
                );
            })
            .collect(Collectors.toList());
    }
    
    public WinnerDTO getWinner() {
        // Check if results are declared
        if (!electionSettingsService.areResultsDeclared()) {
            throw new IllegalStateException("Election results have not been declared yet");
        }
        
        // Find candidate with highest votes
        Candidate winner = candidateRepository.findAll().stream()
            .filter(candidate -> "Completed".equals(candidate.getStatus()))
            .max((c1, c2) -> Long.compare(c1.getVoteCount(), c2.getVoteCount()))
            .orElseThrow(() -> new ResourceNotFoundException("No completed candidates found"));
        
        Auth auth = userRepository.findById(winner.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Winner auth not found"));
        
        // Check for tie
        long winnerVotes = winner.getVoteCount();
        long candidatesWithSameVotes = candidateRepository.findAll().stream()
            .filter(candidate -> "Completed".equals(candidate.getStatus()))
            .mapToLong(Candidate::getVoteCount)
            .filter(votes -> votes == winnerVotes)
            .count();
        
        String message = candidatesWithSameVotes > 1 ? 
            "Election Winner (Tied with " + (candidatesWithSameVotes - 1) + " other candidate(s))" :
            "Election Winner";
        
        return new WinnerDTO(
            winner.getId(),
            auth.getFullName(),
            winner.getPartyName(),
            winner.getSymbol(),
            winner.getVoteCount(),
            message
        );
    }
    
    // Method to check if results can be declared (optional validation)
    public boolean canDeclareResults() {
        // Check if there are completed candidates
        long completedCandidates = candidateRepository.countByStatus("Completed");
        
        // Check if there are any votes cast
        long totalVotes = candidateRepository.findAll().stream()
            .mapToLong(Candidate::getVoteCount)
            .sum();
        
        return completedCandidates > 0 && totalVotes > 0;
    }
}