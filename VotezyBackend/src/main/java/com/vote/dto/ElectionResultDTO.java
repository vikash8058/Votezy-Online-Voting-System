package com.vote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElectionResultDTO {
    private Long candidateId;
    private String candidateName;
    private String partyName;
    private String symbol;
    private Long totalVotes;
    private Integer rank;
}