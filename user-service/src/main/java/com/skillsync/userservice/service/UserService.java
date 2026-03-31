package com.skillsync.userservice.service;

import com.skillsync.userservice.dto.UserRequest;

import com.skillsync.userservice.dto.UserResponse;
import com.skillsync.userservice.entity.UserProfile;
import com.skillsync.userservice.exception.UserAlreadyExistsException;
import com.skillsync.userservice.exception.UserNotFoundException;
import com.skillsync.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import com.skillsync.userservice.dto.UserDto;

/*
 * ================================================================
 * AUTHOR: Kurapati Sai Amulya
 * CLASS: UserService
 * DESCRIPTION:
 * This service class handles the business logic for user management,
 * including profile creation, updates, deletions, and retrieval. 
 * It also maintains mapping between entities and DTOs.
 * ================================================================
 */
@Service
public class UserService {

    /*
     * Logger instance for internal service tracking and debugging
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    /* ================================================================
     * METHOD: createUser
     * DESCRIPTION:
     * Validates if a user already exists and creates a new profile.
     * ================================================================ */
    public UserResponse createUser(UserRequest request) {
        logger.info("Service request: Creating user with email {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("User already exists: {}", request.getEmail());
            throw new UserAlreadyExistsException(request.getEmail());
        }
        UserProfile user = new UserProfile();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setBio(request.getBio());
        user.setSkills(request.getSkills());
        user.setRole(request.getRole());
        user.setAvailability(request.getAvailability());
        return mapToResponse(userRepository.save(user));
    }

    /* ================================================================
     * METHOD: getUserById
     * DESCRIPTION:
     * Retrieves a user profile by its primary key ID.
     * ================================================================ */
    public UserResponse getUserById(Long id) {
        logger.info("Service request: Fetching user by ID {}", id);
        UserProfile user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return mapToResponse(user);
    }

    /* ================================================================
     * METHOD: getUserByEmail
     * DESCRIPTION:
     * Retrieves a user profile by its unique email address.
     * ================================================================ */
    public UserResponse getUserByEmail(String email) {
        logger.info("Service request: Fetching user by email {}", email);
        UserProfile user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(0L));
        return mapToResponse(user);
    }
    /* ================================================================
     * METHOD: getUserByName
     * DESCRIPTION:
     * Retrieves a user profile by searching for their full name.
     * ================================================================ */
    public UserResponse getUserByName(String name) {
        logger.info("Service request: Fetching user by name {}", name);
        UserProfile user = userRepository.findByName(name)
            .orElseThrow(() -> new RuntimeException("User not found with name: " + name));
        return mapToResponse(user);
    }
    /* ================================================================
     * METHOD: createUserFromAuth
     * DESCRIPTION:
     * Synchronizes user profile details received from the Auth Service.
     * ================================================================ */
    public UserResponse createUserFromAuth(UserDto dto) {
        logger.info("Service request: Synchronizing auth profile for {}", dto.getEmail());
        UserProfile user = new UserProfile();
        user.setId(dto.getId());       
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        return mapToResponse(userRepository.save(user));
    }

    /* ================================================================
     * METHOD: getAllUsers
     * DESCRIPTION:
     * Fetches all registered user profiles and maps them to responses.
     * ================================================================ */
    public List<UserResponse> getAllUsers() {
        logger.info("Service request: Fetching all users");
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /* ================================================================
     * METHOD: updateUser
     * DESCRIPTION:
     * Updates an existing user's profile information.
     * ================================================================ */
    public UserResponse updateUser(Long id, UserRequest request) {
        logger.info("Service request: Updating user ID {}", id);
        UserProfile user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setName(request.getName());
        user.setBio(request.getBio());
        user.setSkills(request.getSkills());
        user.setRole(request.getRole());
        user.setAvailability(request.getAvailability());
        return mapToResponse(userRepository.save(user));
    }

    /* ================================================================
     * METHOD: deleteUser
     * DESCRIPTION:
     * Permanently deletes a user profile from the platform.
     * ================================================================ */
    public void deleteUser(Long id) {
        logger.info("Service request: Deleting user ID {}", id);
        if (!userRepository.existsById(id)) {
            logger.warn("Delete failed: User ID {} not found", id);
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    /* ================================================================
     * METHOD: mapToResponse
     * DESCRIPTION:
     * Maps UserProfile entity fields to UserResponse DTO.
     * ================================================================ */
    private UserResponse mapToResponse(UserProfile user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setBio(user.getBio());
        response.setSkills(user.getSkills());
        response.setRole(user.getRole());
        response.setAvailability(user.getAvailability());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}