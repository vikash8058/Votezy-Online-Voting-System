package com.vote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VoterProfileDTO {
    private String fullName;
    private String username;
}