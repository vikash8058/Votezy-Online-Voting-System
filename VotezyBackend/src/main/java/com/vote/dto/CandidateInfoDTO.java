package com.vote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateInfoDTO {
    private Long id;
    private String name;
    private String username;
    private String partyName;
    private String symbol;
    private String status;
    private Long voteCount;
}