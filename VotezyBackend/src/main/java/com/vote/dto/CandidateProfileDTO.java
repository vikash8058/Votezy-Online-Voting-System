package com.vote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateProfileDTO {
	private String fullName;
    private String username;
    private String partyName;
    private String symbol;
    private String status; 
    
    
    public CandidateProfileDTO(String fullName, String username, String partyName, String symbol) {
        this.fullName = fullName;
        this.username = username;
        this.partyName = partyName;
        this.symbol = symbol;
        this.status = "Not Completed"; // Default status
    }
}