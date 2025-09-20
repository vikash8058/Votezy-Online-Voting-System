package com.vote.security;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;
import com.vote.entity.Auth;
import com.vote.repository.UserRepository;
import com.vote.repository.VoterRepository;
import com.vote.repository.CandidateRepository;
import com.vote.repository.ElectionSettingsRepository;

import lombok.Data;

@CrossOrigin
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired UserRepository repo;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtUtil jwtUtil;
    
    
    @Autowired VoterRepository voterRepository;
    @Autowired CandidateRepository candidateRepository;
    @Autowired ElectionSettingsRepository electionSettingsRepository;
    // Hard-coded admin credentials
    private static final String ADMIN_EMAIL = "vikash@gmail.com";
    private static final String ADMIN_PASSWORD = "Admin123@";
    private static final String ADMIN_FULL_NAME = "System Administrator";
    private static final String ADMIN_ROLE = "ADMIN";
    
    @PostMapping("/register")
    @Transactional  // ADD THIS ANNOTATION
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
    	
    	
        // Block ADMIN role registration
        if ("ADMIN".equalsIgnoreCase(req.getRole())) {
            return ResponseEntity.badRequest()
                .body(Collections.singletonMap("message", "ADMIN role cannot be registered. Contact system administrator."));
        }
        // Block registration during an election 
        if (electionSettingsRepository.isElectionRunning()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Collections.singletonMap("message", 
                    "Registration is closed during the election until results are declared."));
        }

        
        // Check if admin email is trying to register as CANDIDATE
        if ("CANDIDATE".equalsIgnoreCase(req.getRole()) && ADMIN_EMAIL.equalsIgnoreCase(req.getEmail())) {
            return ResponseEntity.badRequest()
                .body(Collections.singletonMap("message", "This email is not eligible for candidate registration."));
        }
        
        // Check if user exists with same email and same role
        Optional<Auth> existingUser = repo.findByUsernameAndRole(req.getEmail(), req.getRole());
        if (existingUser.isPresent()) {
            return ResponseEntity.badRequest()
                .body(Collections.singletonMap("message", "User with this email and role already exists"));
        }
        
        // Create and save Auth record
        Auth u = new Auth();
        u.setUsername(req.getEmail());
        u.setPassword(encoder.encode(req.getPassword()));
        u.setFullName(req.getFullName());
        u.setRole(req.getRole());
        Auth savedAuth = repo.save(u);
        
        // IMMEDIATE SYNC: Create corresponding record based on role
        try {
            if ("VOTER".equals(savedAuth.getRole())) {
                voterRepository.insertSingleVoter(savedAuth.getId());
            } else if ("CANDIDATE".equals(savedAuth.getRole())) {
                candidateRepository.insertSingleCandidate(savedAuth.getId());
            }
        } catch (Exception e) {
            // Log the error but don't fail the registration
            System.err.println("Failed to sync " + savedAuth.getRole() + " record for ID " + savedAuth.getId() + ": " + e.getMessage());
            // You could also throw an exception here if you want registration to fail on sync failure
        }
        
        return ResponseEntity.ok(Collections.singletonMap("message", "Registered successfully!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        // Handle ADMIN login with hard-coded credentials
        if ("ADMIN".equalsIgnoreCase(req.getRole())) {
            if (ADMIN_EMAIL.equals(req.getUsername()) && ADMIN_PASSWORD.equals(req.getPassword())) {
                String token = jwtUtil.generateToken(ADMIN_EMAIL, ADMIN_ROLE);
                return ResponseEntity.ok(Map.of(
                    "message", "Admin login successful",
                    "token", token,
                    "fullName", ADMIN_FULL_NAME,
                    "username", ADMIN_EMAIL,
                    "role", ADMIN_ROLE
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("message", "Invalid admin credentials"));
            }
        }
        
        // Handle regular user login (including admin email as VOTER)
        Optional<Auth> userOpt = repo.findByUsernameAndRole(req.getUsername(), req.getRole());
        if (userOpt.isEmpty() || !encoder.matches(req.getPassword(), userOpt.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Collections.singletonMap("message", "Invalid credentials"));
        }
        
        Auth user = userOpt.get();
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        
        return ResponseEntity.ok(Map.of(
            "message", "Login successful",
            "token", token,
            "fullName", user.getFullName(),
            "username", user.getUsername(),
            "role", user.getRole()
        ));
    }
}

// DTOs
@Data
class RegisterRequest {
    private String fullName, email, password, role;
}

@Data
class LoginRequest {
    private String username, password, role;
}