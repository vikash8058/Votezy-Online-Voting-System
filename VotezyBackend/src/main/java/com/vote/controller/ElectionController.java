package com.vote.controller;

import com.vote.dto.ElectionSettingsDTO;
import com.vote.dto.CandidateInfoDTO;
import com.vote.service.ElectionSettingsService;
import com.vote.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class ElectionController {

    @Autowired
    private ElectionSettingsService electionSettingsService;
    
    @Autowired
    private CandidateService candidateService;

    // Election Settings Endpoints
    @GetMapping("/election-settings")
    public ResponseEntity<ElectionSettingsDTO> getElectionSettings() {
        ElectionSettingsDTO settings = electionSettingsService.getSettings();
        return ResponseEntity.ok(settings);
    }

    @PostMapping("/admin/election-settings")
    public ResponseEntity<ElectionSettingsDTO> saveElectionSettings(@RequestBody ElectionSettingsDTO settingsDTO) {
        ElectionSettingsDTO savedSettings = electionSettingsService.saveSettings(settingsDTO);
        return ResponseEntity.ok(savedSettings);
    }
    
    @DeleteMapping("/admin/election-settings/reset")
    public ResponseEntity<Map<String, String>> resetElectionSettings() {
        try {
            electionSettingsService.resetAllSettingsWithTruncate();
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Election settings have been reset successfully. All configuration data has been cleared from the database.");
            response.put("status", "success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to reset election settings: " + e.getMessage());
            errorResponse.put("status", "error");
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PutMapping("/admin/election-settings")
    public ResponseEntity<ElectionSettingsDTO> updateElectionSettings(@RequestBody ElectionSettingsDTO settingsDTO) {
        ElectionSettingsDTO updatedSettings = electionSettingsService.saveSettings(settingsDTO);
        return ResponseEntity.ok(updatedSettings);
    }

    // Public endpoint to get candidates for voting (only completed profiles)
    @GetMapping("/candidates")
    public ResponseEntity<List<CandidateInfoDTO>> getActiveCandidates() {
        List<CandidateInfoDTO> allCandidates = candidateService.getAllCandidates();
        
        // Filter to show only completed candidates
        List<CandidateInfoDTO> activeCandidates = allCandidates.stream()
            .filter(candidate -> "Completed".equals(candidate.getStatus()))
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(activeCandidates);
    }

    // Get candidate details by ID (public for voting)
    @GetMapping("/candidates/{id}")
    public ResponseEntity<CandidateInfoDTO> getCandidateForVoting(@PathVariable Long id) {
        CandidateInfoDTO candidate = candidateService.getCandidateById(id);
        
        if (!"Completed".equals(candidate.getStatus())) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(candidate);
    }
}