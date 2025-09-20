package com.vote.controller;

import com.vote.dto.CandidateInfoDTO;
import com.vote.dto.CandidateProfileDTO;
import com.vote.dto.CandidateProfileUpdateDTO;
import com.vote.dto.CandidateResultsDTO;
import com.vote.dto.CandidateUpdateDTO;
import com.vote.dto.PasswordUpdateRequest;
import com.vote.entity.Auth;
import com.vote.entity.Candidate;
import com.vote.exception.ResourceNotFoundException;
import com.vote.repository.CandidateRepository;
import com.vote.service.CandidateService;
import com.vote.service.ElectionSettingsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class CandidateController {

    @Autowired
    private CandidateService candidateService;
    @Autowired
    private CandidateRepository candidateRepository;
    
    @Autowired
    private ElectionSettingsService electionSettingsService;

    // Candidate Profile Endpoints
    @GetMapping("/candidate/profile")
    public ResponseEntity<CandidateProfileDTO> getCandidateProfile(Authentication auth) {
        CandidateProfileDTO profile = candidateService.getCandidateProfile(auth.getName());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/candidate/profile")
    public ResponseEntity<CandidateProfileDTO> updateCandidateProfile(
            Authentication auth,
            @RequestBody CandidateProfileUpdateDTO updateDTO) {
        CandidateProfileDTO updatedProfile = candidateService.updateCandidateProfile(auth.getName(), updateDTO);
        return ResponseEntity.ok(updatedProfile);
    }

    @PutMapping("/candidate/change-password")
    public ResponseEntity<Map<String, String>> updateCandidatePassword(
            Authentication auth,
            @RequestBody PasswordUpdateRequest request) {
        boolean success = candidateService.updatePassword(
            auth.getName(), 
            request.getCurrentPassword(), 
            request.getNewPassword()
        );
        
        if (success) {
            return ResponseEntity.ok(Collections.singletonMap("message", "Password updated successfully"));
        } else {
            return ResponseEntity.badRequest()
                .body(Collections.singletonMap("message", "Current password is incorrect"));
        }
    }
    
    @GetMapping("/candidate/results")
    public ResponseEntity<CandidateResultsDTO> getCandidateResults(Authentication auth) {
        try {
            // Get candidate info
            Auth candidateAuth = candidateService.getCandidateByUsername(auth.getName());
            Candidate candidate = candidateRepository.findById(candidateAuth.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate record not found"));
            
            // Get vote counts
            long myVotes = candidate.getVoteCount();
            
            // Get all completed candidates and their votes
            List<Candidate> completedCandidates = candidateRepository.findAll().stream()
                .filter(c -> "Completed".equals(c.getStatus()))
                .collect(Collectors.toList());
            
            long totalVotes = completedCandidates.stream()
                .mapToLong(Candidate::getVoteCount)
                .sum();
            
            // Calculate position (rank)
            List<Candidate> sortedCandidates = completedCandidates.stream()
                .sorted((c1, c2) -> Long.compare(c2.getVoteCount(), c1.getVoteCount()))
                .collect(Collectors.toList());
            
            int position = -1;
            for (int i = 0; i < sortedCandidates.size(); i++) {
                if (sortedCandidates.get(i).getId().equals(candidate.getId())) {
                    position = i + 1;
                    break;
                }
            }
            
            // Get election status
            boolean resultsAreDeclared = electionSettingsService.areResultsDeclared();
            String electionStatus = resultsAreDeclared ? "DECLARED" : 
                                    totalVotes > 0 ? "ACTIVE" : "PENDING";
            
            CandidateResultsDTO results = new CandidateResultsDTO(
                myVotes,
                totalVotes,
                position > 0 ? position : null,
                electionStatus,
                resultsAreDeclared || totalVotes > 0
            );
            
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Admin Endpoints for Candidate Management
    @GetMapping("/admin/candidates")
    public ResponseEntity<List<CandidateInfoDTO>> getAllCandidates() {
        List<CandidateInfoDTO> candidates = candidateService.getAllCandidates();
        return ResponseEntity.ok(candidates);
    }

    @GetMapping("/admin/candidates/{id}")
    public ResponseEntity<CandidateInfoDTO> getCandidateById(@PathVariable Long id) {
        CandidateInfoDTO candidate = candidateService.getCandidateById(id);
        return ResponseEntity.ok(candidate);
    }

    @PutMapping("/admin/candidates/{id}")
    public ResponseEntity<Auth> updateCandidate(
            @PathVariable Long id,
            @RequestBody CandidateUpdateDTO updateDTO) {
        Auth updatedCandidate = candidateService.updateCandidate(id, updateDTO);
        return ResponseEntity.ok(updatedCandidate);
    }

    @DeleteMapping("/admin/candidates/{id}")
    public ResponseEntity<Map<String, String>> deleteCandidate(@PathVariable Long id) {
        candidateService.deleteCandidate(id);
        return ResponseEntity.ok(Collections.singletonMap("message", "Candidate deleted successfully"));
    }

    // Statistics endpoints
    @GetMapping("/admin/candidates/stats/total")
    public ResponseEntity<Map<String, Long>> getTotalCandidates() {
        return ResponseEntity.ok(Collections.singletonMap("totalCandidates", candidateService.getTotalCandidates()));
    }

    @GetMapping("/admin/candidates/stats/completed")
    public ResponseEntity<Map<String, Long>> getCompletedProfiles() {
        return ResponseEntity.ok(Collections.singletonMap("completedProfiles", candidateService.getCompletedProfiles()));
    }

    @GetMapping("/admin/candidates/stats/incomplete")
    public ResponseEntity<Map<String, Long>> getIncompleteProfiles() {
        return ResponseEntity.ok(Collections.singletonMap("incompleteProfiles", candidateService.getIncompleteProfiles()));
    }

    // Sync candidate IDs (for admin use)
    @PostMapping("/admin/candidates/sync")
    public ResponseEntity<Map<String, String>> syncCandidateIds() {
        candidateService.syncCandidateIds();
        return ResponseEntity.ok(Collections.singletonMap("message", "Candidate IDs synchronized successfully"));
    }
}