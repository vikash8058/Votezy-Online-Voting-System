package com.vote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VoteResponseDTO {
    private String message;
    private Long voteId;
    private String timestamp;
}