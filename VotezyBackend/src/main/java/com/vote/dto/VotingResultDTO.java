package com.vote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VotingResultDTO {
    private Long candidateId;
    private Long voteCount;
}