package com.vote.dto;

import lombok.Data;

@Data
public class VoterInfoDTO {
    private Long id;
    private String name;
    private String username;
    private Boolean hasVoted;

    public VoterInfoDTO(Long long1, String name, String username, Boolean hasVoted) {
        this.id = long1;
        this.name = name;
        this.username = username;
        this.hasVoted = hasVoted;
    }

    // Getters and setters
 
}