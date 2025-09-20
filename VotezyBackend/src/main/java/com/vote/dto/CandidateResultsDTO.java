package com.vote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResultsDTO {
    private Long myVotes;
    private Long totalVotes;
    private Integer position;
    private String electionStatus; // "PENDING", "ACTIVE", "DECLARED"
    private Boolean canViewResults;
}