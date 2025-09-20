package com.vote.service;

import com.vote.dto.CandidateInfoDTO;
import com.vote.dto.CandidateProfileDTO;
import com.vote.dto.CandidateProfileUpdateDTO;
import com.vote.dto.CandidateUpdateDTO;
import com.vote.entity.Auth;
import com.vote.entity.Candidate;
import com.vote.repository.UserRepository;
import com.vote.repository.CandidateRepository;
import com.vote.exception.DuplicateResourceException;
import com.vote.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CandidateService {

    @Autowired
    private UserRepository authRepository;

    @Autowired
    private CandidateRepository candidateRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Get candidate by username
    public Auth getCandidateByUsername(String username) {
        return authRepository.findByUsernameAndRole(username, "CANDIDATE")
            .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));
    }

    // Sync candidate IDs from Auth table
    public void syncCandidateIds() {
        candidateRepository.insertCandidateIdsFromAuth();
    }

    // Get candidate profile for display
    public CandidateProfileDTO getCandidateProfile(String username) {
        Auth auth = getCandidateByUsername(username);
        Candidate candidate = candidateRepository.findById(auth.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Candidate record not found"));
        
        return new CandidateProfileDTO(
            auth.getFullName(),
            auth.getUsername(),
            candidate.getPartyName(),
            candidate.getSymbol(),
            candidate.getStatus() // Include the actual status from database
        );
    }

    // Update candidate profile
    @Transactional
    public CandidateProfileDTO updateCandidateProfile(String username, CandidateProfileUpdateDTO updateDTO) {
        Auth auth = getCandidateByUsername(username);
        Candidate candidate = candidateRepository.findById(auth.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Candidate record not found"));
        
        // Check if profile is already completed - prevent further updates
        if ("Completed".equalsIgnoreCase(candidate.getStatus())) {
            throw new IllegalStateException("Profile update not allowed. Candidate profile is already completed.");
        }

        // Validate party name uniqueness
        if (updateDTO.getPartyName() != null && !updateDTO.getPartyName().isBlank()) {
            if (candidateRepository.existsByPartyNameAndIdNot(updateDTO.getPartyName(), candidate.getId())) {
                throw new DuplicateResourceException("Party name '" + updateDTO.getPartyName() + "' is already taken");
            }
            candidate.setPartyName(updateDTO.getPartyName());
        }

        // Validate symbol uniqueness
        if (updateDTO.getSymbol() != null && !updateDTO.getSymbol().isBlank()) {
            if (candidateRepository.existsBySymbolAndIdNot(updateDTO.getSymbol(), candidate.getId())) {
                throw new DuplicateResourceException("Symbol '" + updateDTO.getSymbol() + "' is already taken");
            }
            candidate.setSymbol(updateDTO.getSymbol());
        }

        // Update status to completed if both party name and symbol are provided
        if (candidate.getPartyName() != null && !candidate.getPartyName().isBlank() &&
            candidate.getSymbol() != null && !candidate.getSymbol().isBlank()) {
            candidate.setStatus("Completed");
        }

        candidateRepository.save(candidate);

        return new CandidateProfileDTO(
            auth.getFullName(),
            auth.getUsername(),
            candidate.getPartyName(),
            candidate.getSymbol(),
            candidate.getStatus() // Return the updated status
        );
    }

    // Password update functionality
    public boolean updatePassword(String username, String currentPassword, String newPassword) {
        Auth candidate = authRepository.findByUsernameAndRole(username, "CANDIDATE")
            .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));
        
        if (!passwordEncoder.matches(currentPassword, candidate.getPassword())) {
            return false;
        }
        
        candidate.setPassword(passwordEncoder.encode(newPassword));
        authRepository.save(candidate);
        return true;
    }

    // Admin functionality - Get all candidates
    public List<CandidateInfoDTO> getAllCandidates() {
        List<Auth> candidates = authRepository.findByRole("CANDIDATE");
        List<CandidateInfoDTO> result = new ArrayList<>();
        
        for (Auth auth : candidates) {
            Candidate candidate = candidateRepository.findById(auth.getId())
                .orElse(null);
            
            if (candidate != null) {
                CandidateInfoDTO dto = new CandidateInfoDTO(
                    auth.getId(),
                    auth.getFullName(),
                    auth.getUsername(),
                    candidate.getPartyName(),
                    candidate.getSymbol(),
                    candidate.getStatus(),
                    candidate.getVoteCount()
                );
                result.add(dto);
            }
        }
        return result;
    }

    // Admin functionality - Get candidate by ID
    public CandidateInfoDTO getCandidateById(Long id) {
        Auth auth = authRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id " + id));
        
        if (!"CANDIDATE".equals(auth.getRole())) {
            throw new IllegalArgumentException("User with id " + id + " is not a CANDIDATE");
        }
        
        Candidate candidate = candidateRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Candidate record not found"));

        return new CandidateInfoDTO(
            auth.getId(),
            auth.getFullName(),
            auth.getUsername(),
            candidate.getPartyName(),
            candidate.getSymbol(),
            candidate.getStatus(),
            candidate.getVoteCount()
        );
    }

    // Admin functionality - Update candidate
    @Transactional
    public Auth updateCandidate(Long id, CandidateUpdateDTO updateDTO) {
        Auth auth = authRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id " + id));

        if (!"CANDIDATE".equalsIgnoreCase(auth.getRole())) {
            throw new IllegalArgumentException("User with id " + id + " is not a CANDIDATE");
        }

        // Update full name if provided
        if (updateDTO.getFullName() != null && !updateDTO.getFullName().isBlank()) {
            auth.setFullName(updateDTO.getFullName());
        }

        // Update username if provided and different
        if (updateDTO.getUsername() != null && !updateDTO.getUsername().isBlank()
                && !updateDTO.getUsername().equals(auth.getUsername())) {

            if (authRepository.existsByUsername(updateDTO.getUsername())) {
                throw new IllegalArgumentException("Username '" + updateDTO.getUsername() + "' is already taken");
            }
            auth.setUsername(updateDTO.getUsername());
        }

        return authRepository.save(auth);
    }

    // Admin functionality - Delete candidate
    @Transactional
    public void deleteCandidate(Long id) {
        Auth candidate = authRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id " + id));

        if (!"CANDIDATE".equals(candidate.getRole())) {
            throw new IllegalArgumentException("User with id " + id + " is not a CANDIDATE");
        }

        Candidate candidateRecord = candidateRepository.findById(id).orElse(null);
        
        // Check if candidate has received any votes
        if (candidateRecord != null && candidateRecord.getVoteCount() > 0) {
            throw new RuntimeException("Cannot delete candidate who has received votes");
        }

        authRepository.delete(candidate);
    }

    // Statistics
    public long getTotalCandidates() {
        return candidateRepository.count();
    }
    
    public long getCompletedProfiles() {
        return candidateRepository.countByStatus("Completed");
    }
    
    public long getIncompleteProfiles() {
        return candidateRepository.countByStatus("Not Completed");
    }
}