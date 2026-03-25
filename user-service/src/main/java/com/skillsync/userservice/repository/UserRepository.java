package com.skillsync.userservice.repository;

import com.skillsync.userservice.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<UserProfile> findByName(String name); 
}