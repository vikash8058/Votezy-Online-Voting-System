package com.vote.dto;

import lombok.Data;

@Data
public class ElectionSettingsDTO {
    private String name;
    private String type;
    private String startDate;
    private String endDate;
    private String startTime;
    private String endTime;
    private String status;
}