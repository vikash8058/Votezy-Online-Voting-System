package com.vote.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ElectionSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type;
    private String startDate; // yyyy-MM-dd
    private String endDate;
    private String startTime; // HH:mm
    private String endTime;
    private String status; // active/inactive
    private String resultStatus; // not_declared/declared
}