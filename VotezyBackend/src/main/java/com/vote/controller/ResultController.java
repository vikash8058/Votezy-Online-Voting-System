package com.vote.controller;

import com.vote.dto.ElectionResultDTO;
import com.vote.dto.WinnerDTO;
import com.vote.service.ResultService;
import com.vote.service.ElectionSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class ResultController {

    @Autowired
    private ResultService resultService;
    
    @Autowired
    private ElectionSettingsService electionSettingsService;

    // ADMIN ENDPOINTS
    
    /**
     * Admin declares election results
     */
    @PostMapping("/admin/declare-results")
    public ResponseEntity<Map<String, String>> declareResults() {
        try {
            if (!resultService.canDeclareResults()) {
                return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("message", "Cannot declare results: No completed candidates or no votes cast"));
            }
            
            electionSettingsService.declareResults();
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Election results have been declared successfully");
            response.put("status", "declared");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Collections.singletonMap("message", e.getMessage()));
        }
    }
    
    /**
     * Check if results are declared
     */
    @GetMapping("/admin/result-status")
    public ResponseEntity<Map<String, Object>> getResultStatus() {
        boolean declared = electionSettingsService.areResultsDeclared();
        String status = electionSettingsService.getResultStatus();
        boolean canDeclare = resultService.canDeclareResults();
        
        Map<String, Object> response = new HashMap<>();
        response.put("declared", declared);
        response.put("status", status);
        response.put("canDeclare", canDeclare);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/isResult/declared")
    public ResponseEntity<?> checkResultsStatus() {
        try {
            boolean declared = electionSettingsService.areResultsDeclared();
            return ResponseEntity.ok(Map.of("resultsDeclared", declared));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to fetch results status",
                            "details", e.getMessage()
                    ));
        }
    }


    // PUBLIC ENDPOINTS (accessible after results are declared)
    
    /**
     * Get election results table (sorted by votes descending)
     */
    @GetMapping("/election-results")
    public ResponseEntity<List<ElectionResultDTO>> getElectionResults() {
        try {
            List<ElectionResultDTO> results = resultService.getElectionResults();
            return ResponseEntity.ok(results);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).build(); // Forbidden - results not declared
        } catch (Exception e) {
            return ResponseEntity.status(500).build(); // Internal server error
        }
    }
    
    /**
     * Get winner details
     */
    @GetMapping("/election-winner")
    public ResponseEntity<WinnerDTO> getElectionWinner() {
        try {
            WinnerDTO winner = resultService.getWinner();
            return ResponseEntity.ok(winner);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).build(); // Forbidden - results not declared
        } catch (Exception e) {
            return ResponseEntity.status(500).build(); // Internal server error
        }
    }
    
    /**
     * Check if results are publicly available
     */
    @GetMapping("/results-available")
    public ResponseEntity<Map<String, Boolean>> areResultsAvailable() {
        boolean available = electionSettingsService.areResultsDeclared();
        return ResponseEntity.ok(Collections.singletonMap("available", available));
    }
    
    // ADMIN ANALYTICS (additional useful endpoints)
    
    /**
     * Get detailed election statistics for admin
     */
    @GetMapping("/admin/election-stats")
    public ResponseEntity<Map<String, Object>> getElectionStats() {
        try {
            List<ElectionResultDTO> results = electionSettingsService.areResultsDeclared() 
                ? resultService.getElectionResults() 
                : null;
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("resultsAvailable", electionSettingsService.areResultsDeclared());
            stats.put("canDeclareResults", resultService.canDeclareResults());
            stats.put("totalCandidates", results != null ? results.size() : 0);
            
            if (results != null && !results.isEmpty()) {
                long totalVotes = results.stream().mapToLong(ElectionResultDTO::getTotalVotes).sum();
                stats.put("totalVotesCast", totalVotes);
                stats.put("winnerVotes", results.get(0).getTotalVotes());
                stats.put("winnerName", results.get(0).getCandidateName());
            }
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}