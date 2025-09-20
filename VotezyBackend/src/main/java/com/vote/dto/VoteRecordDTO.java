package com.vote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VoteRecordDTO {
    private Long voterId;
    private Long candidateId;
    private String candidateName;
    private String partyName;
    private String partySymbol;
}
