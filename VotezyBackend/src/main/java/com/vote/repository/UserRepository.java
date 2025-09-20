package com.vote.repository;

import com.vote.entity.Auth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Auth, Long> {
    
    // This method should return Optional to handle cases where no user is found
    @Query("SELECT u FROM Auth u WHERE u.username = :username")
    Optional<Auth> findByUsername(@Param("username") String username);
    
    @Query("SELECT id FROM Auth WHERE username=:username")
    Long findByUsername1(String username);
    
    // This method returns all users with the given username (multiple roles possible)
    @Query("SELECT u FROM Auth u WHERE u.username = :username")
    List<Auth> findAllByUsername(@Param("username") String username);
    
    // This method finds user by username and role combination (should be unique)
    Optional<Auth> findByUsernameAndRole(String username, String role);
    
    // Find all users by role
    List<Auth> findByRole(String role);
    
    // Check if username exists
    boolean existsByUsername(String username);
    
}