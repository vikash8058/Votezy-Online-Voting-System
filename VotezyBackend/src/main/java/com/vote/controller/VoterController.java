package com.vote.controller;

import com.vote.dto.PasswordUpdateRequest;
import com.vote.dto.VoteRequestDTO;
import com.vote.dto.VoteResponseDTO;
import com.vote.dto.VoterInfoDTO;
import com.vote.dto.VoterProfileDTO;
import com.vote.dto.VoterUpdateDTO;
import com.vote.dto.VotingResultDTO;
import com.vote.entity.Auth;
import com.vote.service.VoteService;
import com.vote.service.VoterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class VoterController {

    @Autowired
    private VoterService voterService;
    
    @Autowired
    private VoteService voteService;

    // Voter Profile Endpoints
    @GetMapping("/profile")
    public ResponseEntity<VoterProfileDTO> getVoterProfile(Authentication auth) {
        Auth voter = voterService.getVoterByUsername(auth.getName());
        VoterProfileDTO profile = new VoterProfileDTO(voter.getFullName(), voter.getUsername());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/update-password")
    public ResponseEntity<Map<String, String>> updatePassword(
            Authentication auth,
            @RequestBody PasswordUpdateRequest request) {
        boolean success = voterService.updatePassword(
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
    
    //get vote record
    
    @GetMapping("/vote-record/{username}")
    public ResponseEntity<?> getVoteRecord(@PathVariable String username) {
        return voteService.getVoteRecord(username)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok(
                        Map.of("message", "No vote record found for this voter.")
                ));
    }

    // Voting Endpoints
    @PostMapping("/cast-vote")
    public ResponseEntity<VoteResponseDTO> castVote(
            Authentication auth,
            @RequestBody VoteRequestDTO voteRequest) {
        VoteResponseDTO response = voterService.castVote(auth.getName(), voteRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/voting-status")
    public ResponseEntity<Map<String, Boolean>> getVotingStatus(Authentication auth) {
        boolean hasVoted = voterService.hasVoterVoted(auth.getName());
        return ResponseEntity.ok(Collections.singletonMap("hasVoted", hasVoted));
    }

    // Admin Endpoints for Voter Management
    @GetMapping("/admin/voters")
    public ResponseEntity<List<VoterInfoDTO>> getAllVoters() {
        List<VoterInfoDTO> voters = voterService.getVoterDetails();
        return ResponseEntity.ok(voters);
    }

    @GetMapping("/admin/voters/{id}")
    public ResponseEntity<VoterInfoDTO> getVoterById(@PathVariable Long id) {
        VoterInfoDTO voter = voterService.getVoterById(id);
        return ResponseEntity.ok(voter);
    }

    @PutMapping("/admin/voters/{id}")
    public ResponseEntity<Auth> updateVoter(
            @PathVariable Long id,
            @RequestBody VoterUpdateDTO updateDTO) {
        Auth updatedVoter = voterService.updateVoter(id, updateDTO);
        return ResponseEntity.ok(updatedVoter);
    }

    @DeleteMapping("/admin/voters/{id}")
    public ResponseEntity<Map<String, String>> deleteVoter(@PathVariable Long id) {
        voterService.deleteVoter(id);
        return ResponseEntity.ok(Collections.singletonMap("message", "Voter deleted successfully"));
    }
    

    // Statistics endpoints
 // Add these corrected methods to your VoterController.java

	 // Statistics endpoints - Fixed to use proper admin paths
	 @GetMapping("/admin/total-voters")
	 public ResponseEntity<Map<String, Long>> getTotalVoters() {
	     long total = voterService.getTotalVoters();
	     return ResponseEntity.ok(Collections.singletonMap("totalVoters", total));
	 }
	
	 @GetMapping("/admin/voted-successfully")
	 public ResponseEntity<Map<String, Long>> getVotedSuccessfully() {
	     long votedCount = voterService.getVotedSuccessfully();
	     return ResponseEntity.ok(Collections.singletonMap("votedSuccessfully", votedCount));
	 }
	
	 @GetMapping("/admin/havent-voted")
	 public ResponseEntity<Map<String, Long>> getHaventVoted() {
	     long haventVotedCount = voterService.getHaventVoted();
	     return ResponseEntity.ok(Collections.singletonMap("haventVoted", haventVotedCount));
	 }
	
	 // Keep the existing public endpoints for backward compatibility
	 @GetMapping("/total-voters")
	 public ResponseEntity<Map<String, Long>> getTotalVotersPublic() {
	     long total = voterService.getTotalVoters();
	     return ResponseEntity.ok(Collections.singletonMap("totalVoters", total));
	 }
	
	 @GetMapping("/voted-successfully")
	 public ResponseEntity<Map<String, Long>> getVotedSuccessfullyPublic() {
	     long votedCount = voterService.getVotedSuccessfully();
	     return ResponseEntity.ok(Collections.singletonMap("votedSuccessfully", votedCount));
	 }
	
	 @GetMapping("/havent-voted")
	 public ResponseEntity<Map<String, Long>> getHaventVotedPublic() {
	     long haventVotedCount = voterService.getHaventVoted();
	     return ResponseEntity.ok(Collections.singletonMap("haventVoted", haventVotedCount));
	 }

    // Voting Results (Admin only)
    @GetMapping("/voting-results")
    public ResponseEntity<List<VotingResultDTO>> getVotingResults() {
        List<VotingResultDTO> results = voterService.getVotingResults();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/total-votes-cast")
    public ResponseEntity<Map<String, Long>> getTotalVotesCast() {
        return ResponseEntity.ok(Collections.singletonMap("totalVotesCast", voterService.getTotalVotesCast()));
    }

    @GetMapping("/candidate/{candidateId}/votes")
    public ResponseEntity<Map<String, Long>> getVotesForCandidate(@PathVariable Long candidateId) {
        long votes = voterService.getVotesForCandidate(candidateId);
        return ResponseEntity.ok(Collections.singletonMap("voteCount", votes));
    }

    // Sync voters (Admin only)
    @PostMapping("/admin/voters/sync")
    public ResponseEntity<Map<String, String>> syncVoterIds() {
        voterService.syncVoterIds();
        return ResponseEntity.ok(Collections.singletonMap("message", "Voter IDs synchronized successfully"));
    }
}