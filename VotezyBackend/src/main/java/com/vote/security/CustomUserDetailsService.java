package com.vote.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.vote.entity.Auth;
import com.vote.repository.UserRepository;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired 
    UserRepository repo;
    
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Handle the case where multiple users exist with same username but different roles
        List<Auth> users = repo.findAllByUsername(username); // You'll need to create this method
        
        if (users.isEmpty()) {
            throw new UsernameNotFoundException("User Not Found");
        }
        
        // If multiple users exist, prioritize VOTER role (or any other logic you prefer)
        Auth user = users.stream()
                .filter(u -> "VOTER".equals(u.getRole()))
                .findFirst()
                .orElse(users.get(0)); // fallback to first user if no VOTER found
        
        return User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .roles(user.getRole()) // Admin, Voter, Candidate
            .build();
    }
    
    // Method to load user by username and role specifically
    public UserDetails loadUserByUsernameAndRole(String username, String role) throws UsernameNotFoundException {
        Auth user = repo.findByUsernameAndRole(username, role)
            .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username + " and role: " + role));
        
        return User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .roles(user.getRole())
            .build();
    }
}