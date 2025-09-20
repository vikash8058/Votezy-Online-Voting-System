package com.vote.service;

import com.vote.dto.VoteRecordDTO;
import com.vote.entity.Auth;
import com.vote.entity.Candidate;
import com.vote.entity.Vote;
import com.vote.repository.CandidateRepository;
import com.vote.repository.UserRepository;
import com.vote.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.Authenticator;
import java.util.Optional;

@Service
public class VoteService {

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private CandidateRepository candidateRepository;
    @Autowired
    private UserRepository userRepository;

    public Optional<VoteRecordDTO> getVoteRecord(String username) {
    	Long voterId=userRepository.findByUsername1(username);
    	
        Optional<Vote> voteOpt = voteRepository.findByVoterId(voterId);

        if (voteOpt.isPresent()) {
            Vote vote = voteOpt.get();
            Candidate candidate = candidateRepository.findById(vote.getCandidateId())
                    .orElse(null);
            Auth candi=userRepository.findById(vote.getCandidateId())
            		.orElse(null);
            
            if (candidate != null) {
                VoteRecordDTO dto = new VoteRecordDTO(
                        vote.getVoterId(),
                        vote.getCandidateId(),
                        candi.getFullName(),
                        candidate.getPartyName(),
                        candidate.getSymbol()
                );
                return Optional.of(dto);
            }
        }

        return Optional.empty();
    }
}
