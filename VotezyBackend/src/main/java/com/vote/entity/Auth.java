package com.vote.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Entity
@Data
@Table(
  uniqueConstraints = @UniqueConstraint(columnNames = {"username", "role"})
)
public class Auth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "username is required")
    @Email(message = "Invalid username format, username should be abc@gmail.com")
    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String role;

    @OneToOne(mappedBy = "auth", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonManagedReference
    private Voter voter;

    @OneToOne(mappedBy = "auth", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonManagedReference
    private Candidate candidate;
}