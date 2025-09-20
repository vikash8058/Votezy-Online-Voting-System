
package com.vote.controller;

import com.vote.dto.VoteRequestDTO;
import com.vote.dto.VoteResponseDTO;
import com.vote.dto.VotingResultDTO;
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
@RequestMapping("/api/vote")
public class VoteController {

    @Autowired
    private VoterService voterService;

    // Cast Vote
    @PostMapping("/cast")
    public ResponseEntity<VoteResponseDTO> castVote(
            Authentication auth,
            @RequestBody VoteRequestDTO voteRequest) {
        VoteResponseDTO response = voterService.castVote(auth.getName(), voteRequest);
        return ResponseEntity.ok(response);
    }

    // Check if voter has already voted
    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> getVotingStatus(Authentication auth) {
        boolean hasVoted = voterService.hasVoterVoted(auth.getName());
        return ResponseEntity.ok(Collections.singletonMap("hasVoted", hasVoted));
    }

    // Get voting results (Admin only)
    @GetMapping("/results")
    public ResponseEntity<List<VotingResultDTO>> getVotingResults() {
        List<VotingResultDTO> results = voterService.getVotingResults();
        return ResponseEntity.ok(results);
    }

    // Get total votes cast (Admin only)
    @GetMapping("/total")
    public ResponseEntity<Map<String, Long>> getTotalVotesCast() {
        return ResponseEntity.ok(Collections.singletonMap("totalVotesCast", voterService.getTotalVotesCast()));
    }

    // Get votes for specific candidate (Admin only)
    @GetMapping("/candidate/{candidateId}/count")
    public ResponseEntity<Map<String, Long>> getVotesForCandidate(@PathVariable Long candidateId) {
        long votes = voterService.getVotesForCandidate(candidateId);
        return ResponseEntity.ok(Collections.singletonMap("voteCount", votes));
    }
}