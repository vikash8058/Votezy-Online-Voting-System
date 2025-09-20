package com.vote.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "vote")
public class Vote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "voter_id", nullable = false)
    private Long voterId;
    
    @Column(name = "candidate_id", nullable = false)
    private Long candidateId;
    
    @Column(name = "vote_timestamp", nullable = false)
    private LocalDateTime voteTimestamp;
    
    @PrePersist
    protected void onCreate() {
        voteTimestamp = LocalDateTime.now();
    }
}