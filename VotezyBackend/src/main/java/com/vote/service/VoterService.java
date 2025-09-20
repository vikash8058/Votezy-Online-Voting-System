package com.vote.service;

import com.vote.dto.VoteRequestDTO;
import com.vote.dto.VoteResponseDTO;
import com.vote.dto.VotingResultDTO;
import com.vote.dto.VoterInfoDTO;
import com.vote.dto.VoterUpdateDTO;
import com.vote.entity.Auth;
import com.vote.entity.Vote;
import com.vote.entity.Voter;
import com.vote.entity.Candidate;
import com.vote.repository.UserRepository;
import com.vote.repository.VoteRepository;
import com.vote.repository.VoterRepository;
import com.vote.repository.CandidateRepository;
import com.vote.exception.ElectionResultAlreadyDeclaredException;
import com.vote.exception.ResourceNotFoundException;
import com.vote.exception.VoteNotAllowedException;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VoterService {

    @Autowired
    private UserRepository authRepository;

    @Autowired
    private VoterRepository voterRepository;
    
    @Autowired
    private VoteRepository voteRepository;
    
    @Autowired
    private CandidateRepository candidateRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private ElectionSettingsService electionSettingsService;

    // Existing methods...
    public Auth getVoterByUsername(String username) {
        return authRepository.findByUsernameAndRole(username, "VOTER")
            .orElseThrow(() -> new ResourceNotFoundException("Voter not found"));
    }

    public boolean updatePassword(String username, String currentPassword, String newPassword) {
        Auth voter = authRepository.findByUsernameAndRole(username, "VOTER")
            .orElseThrow(() -> new ResourceNotFoundException("Voter not found"));
        
        if (!passwordEncoder.matches(currentPassword, voter.getPassword())) {
            return false;
        }
        
        voter.setPassword(passwordEncoder.encode(newPassword));
        authRepository.save(voter);
        return true;
    }

    public void syncVoterIds() {
        voterRepository.insertVoterIdsFromAuth();
    }

    // UPDATED VOTING FUNCTIONALITY - Now updates candidate vote count
    @Transactional
    public VoteResponseDTO castVote(String username, VoteRequestDTO voteRequest) {
        // Get voter by username
        Auth auth = getVoterByUsername(username);
        
        // Check if voter record exists
        Voter voter = voterRepository.findById(auth.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Voter record not found"));
        if ("declared".equals(electionSettingsService.getResultStatus())) {
            throw new ElectionResultAlreadyDeclaredException("Election results have already been declared");
        }
        
        // Check if voter has already voted
        if (Boolean.TRUE.equals(voter.getHasVoted())) {
            throw new VoteNotAllowedException("You have already cast your vote");
        }
        
        // Double check with Vote table
        if (voteRepository.existsByVoterId(voter.getId())) {
            throw new IllegalStateException("Vote record already exists for this voter");
        }
        
        // Validate candidate exists and profile is completed
        Candidate candidate = candidateRepository.findById(voteRequest.getCandidateId())
            .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));
        
        if (!"Completed".equals(candidate.getStatus())) {
            throw new IllegalStateException("Cannot vote for candidate with incomplete profile");
        }
        
        // Create vote record
        Vote vote = new Vote();
        vote.setVoterId(voter.getId());
        vote.setCandidateId(voteRequest.getCandidateId());
        
        // Save vote
        Vote savedVote = voteRepository.save(vote);
        
        // Update voter hasVoted flag
        voter.setHasVoted(true);
        voterRepository.save(voter);
        
        // Update candidate vote count
        candidate.setVoteCount(candidate.getVoteCount() + 1);
        candidateRepository.save(candidate);
        
        // Return response
        return new VoteResponseDTO(
            "Vote cast successfully", 
            savedVote.getId(),
            savedVote.getVoteTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }
    
    // Check if voter has voted
    public boolean hasVoterVoted(String username) {
        Auth auth = getVoterByUsername(username);
        Voter voter = voterRepository.findById(auth.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Voter record not found"));
        return Boolean.TRUE.equals(voter.getHasVoted());
    }
    
    // Get voting results
    public List<VotingResultDTO> getVotingResults() {
        List<Object[]> results = voteRepository.getVotingResults();
        return results.stream()
            .map(result -> new VotingResultDTO(
                (Long) result[0],  // candidateId
                (Long) result[1]   // voteCount
            ))
            .collect(Collectors.toList());
    }
    
    // Get total votes cast
    public long getTotalVotesCast() {
        return voteRepository.getTotalVotesCast();
    }
    
    // Get votes for specific candidate
    public long getVotesForCandidate(Long candidateId) {
        return voteRepository.countByCandidateId(candidateId);
    }

    // Existing methods remain the same...
    @Transactional
    public void deleteVoter(Long id) {
        Auth voter = authRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voter not found with id " + id));

        Voter votingStatus = voterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role is different, not VOTER whose id is :" + id));

        if (Boolean.TRUE.equals(votingStatus.getHasVoted())) {
            throw new RuntimeException("Cannot delete voter who has already cast a vote");
        }

        authRepository.delete(voter);
    }

    public List<VoterInfoDTO> getVoterDetails() {
        List<Auth> voters = authRepository.findByRole("VOTER");
        List<VoterInfoDTO> result = new ArrayList<>();
        for (Auth auth : voters) {
            Boolean hasVoted = voterRepository.findById(auth.getId())
                    .map(Voter::getHasVoted)
                    .orElse(false);
            VoterInfoDTO dto = new VoterInfoDTO(auth.getId(), auth.getFullName(), auth.getUsername(), hasVoted);
            result.add(dto);
        }
        return result;
    }

    public VoterInfoDTO getVoterById(Long id) {
        Auth auth = authRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voter not found with id " + id));
        Boolean hasVoted = voterRepository.findById(id)
                .map(Voter::getHasVoted)
                .orElse(false);

        return new VoterInfoDTO(auth.getId(), auth.getFullName(), auth.getUsername(), hasVoted);
    }

    @Transactional
    public Auth updateVoter(Long id, VoterUpdateDTO updateDTO) {
        Auth auth = authRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voter not found with id " + id));

        if (!"VOTER".equalsIgnoreCase(auth.getRole())) {
            throw new IllegalArgumentException("User with id " + id + " is not a VOTER");
        }

        Voter voter = voterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voting record not found for id " + id));

        if (Boolean.TRUE.equals(voter.getHasVoted())) {
            throw new IllegalStateException("Cannot edit voter details after vote has been casted");
        }

        if (updateDTO.getFullName() != null && !updateDTO.getFullName().isBlank()) {
            auth.setFullName(updateDTO.getFullName());
        }

        if (updateDTO.getUsername() != null && !updateDTO.getUsername().isBlank()
                && !updateDTO.getUsername().equals(auth.getUsername())) {

            if (authRepository.existsByUsername(updateDTO.getUsername())) {
                throw new IllegalArgumentException("Username '" + updateDTO.getUsername() + "' is already taken");
            }
            auth.setUsername(updateDTO.getUsername());
        }

        return authRepository.save(auth);
    }

    // Statistics methods
    public long getTotalVoters() {
        return voterRepository.count();
    }
    
    public long getVotedSuccessfully() {
        return voterRepository.countByHasVoted(true);
    }
    
    public long getHaventVoted() {
        return voterRepository.countByHasVoted(false);
    }
}