package com.vote.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "candidate")
public class Candidate {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    @JsonBackReference
    private Auth auth;

    @Column(name = "party_name", unique = true)
    private String partyName;

    @Column(name = "symbol", unique = true)
    private String symbol;

    @Column(name = "vote_count", nullable = false)
    private Long voteCount = 0L;

    @Column(name = "status")
    private String status = "Profile Incomplete"; // Set default value
}