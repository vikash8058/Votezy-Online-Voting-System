package com.vote.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import com.vote.repository.CandidateRepository;
import com.vote.repository.VoterRepository;

@Service
public class DatabaseSyncService implements ApplicationRunner {

    @Autowired
    private VoterRepository voterRepository;
    
    @Autowired
    private CandidateRepository candidateRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Sync voter records from Auth table
        voterRepository.insertVoterIdsFromAuth();
        
        // Sync candidate records from Auth table
        candidateRepository.insertCandidateIdsFromAuth();
        
        System.out.println("Database synchronization completed successfully!");
    }
}