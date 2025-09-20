package com.vote.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "voter")
public class Voter {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    @JsonBackReference
    private Auth auth;

    @Column(name = "has_voted", nullable = false)
    private Boolean hasVoted = false;
}