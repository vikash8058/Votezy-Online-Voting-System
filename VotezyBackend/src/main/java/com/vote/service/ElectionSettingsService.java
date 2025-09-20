package com.vote.service;

import com.vote.dto.ElectionSettingsDTO;
import com.vote.dto.VoteRecordDTO;
import com.vote.entity.ElectionSettings;
import com.vote.repository.ElectionSettingsRepository;
import com.vote.repository.VoteRepository;
import com.vote.exception.ResourceNotFoundException;
import com.vote.exception.ElectionResultAlreadyDeclaredException;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ElectionSettingsService {

    @Autowired
    private ElectionSettingsRepository repo;
    
    @Autowired
    private VoteRepository voteRepository;
    
    @Transactional
    public void resetAllSettingsWithTruncate() {
        try {
            repo.truncateTable();
        } catch (Exception e) {
            throw new RuntimeException("Failed to truncate election settings table: " + e.getMessage(), e);
        }
    }

    public ElectionSettingsDTO getSettings() {
        ElectionSettings settings = repo.findAll().stream().findFirst().orElse(null);
        if (settings == null) return null;
        ElectionSettingsDTO dto = new ElectionSettingsDTO();
        dto.setName(settings.getName());
        dto.setType(settings.getType());
        dto.setStartDate(settings.getStartDate());
        dto.setEndDate(settings.getEndDate());
        dto.setStartTime(settings.getStartTime());
        dto.setEndTime(settings.getEndTime());
        dto.setStatus(settings.getStatus());
        return dto;
    }

    public ElectionSettingsDTO saveSettings(ElectionSettingsDTO dto) {
        repo.deleteAll(); // Only one settings row
        ElectionSettings settings = new ElectionSettings();
        settings.setName(dto.getName());
        settings.setType(dto.getType());
        settings.setStartDate(dto.getStartDate());
        settings.setEndDate(dto.getEndDate());
        settings.setStartTime(dto.getStartTime());
        settings.setEndTime(dto.getEndTime());
        settings.setStatus(dto.getStatus());
        settings.setResultStatus("not_declared"); // Default value
        repo.save(settings);
        return dto;
    }
    
    // NEW METHODS FOR RESULT DECLARATION
    @Transactional
    public void declareResults() {
        ElectionSettings settings = repo.findAll().stream().findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Election settings not found"));
        
        if ("declared".equals(settings.getResultStatus())) {
            throw new ElectionResultAlreadyDeclaredException("Election results have already been declared");
        }
        
        settings.setResultStatus("declared");
        repo.save(settings);
    }
    
    public boolean areResultsDeclared() {
        ElectionSettings settings = repo.findAll().stream().findFirst().orElse(null);
        return settings != null && "declared".equals(settings.getResultStatus());
    }
    
    public String getResultStatus() {
        ElectionSettings settings = repo.findAll().stream().findFirst().orElse(null);
        return settings != null ? settings.getResultStatus() : "not_declared";
    }
    
  
}