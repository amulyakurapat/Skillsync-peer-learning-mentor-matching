package com.skillsync.userservice.controller;

import com.skillsync.userservice.dto.UserRequest;

import com.skillsync.userservice.dto.UserResponse;
import com.skillsync.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.skillsync.userservice.dto.UserDto;
/*
 * ================================================================
 * AUTHOR: Kurapati Sai Amulya
 * CLASS: UserController
 * DESCRIPTION:
 * This controller manages user-related operations, including 
 * user creation, retrieval, updates, and deletion. It also 
 * provides internal endpoints for authentication service integration.
 * ================================================================
 */
@RestController
@RequestMapping("/users")
public class UserController {

    /*
     * Logger instance for tracking API calls and monitoring user activities
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /* ================================================================
     * METHOD: createUser
     * DESCRIPTION:
     * Creates a new user profile by validating inputs and delegating 
     * the creation logic to the UserService.
     * ================================================================ */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        logger.info("Creating new user with email: {}", request.getEmail());
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /* ================================================================
     * METHOD: getUserById
     * DESCRIPTION:
     * Fetches a single user's details based on their unique database ID.
     * ================================================================ */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        logger.info("Fetching profile for user ID: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /* ================================================================
     * METHOD: getUserByEmail
     * DESCRIPTION:
     * Retrieves a user profile using their registered email address.
     * ================================================================ */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        logger.info("Fetching profile for email: {}", email);
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    /* ================================================================
     * METHOD: getAllUsers
     * DESCRIPTION:
     * Returns a list of all registered users in the platform.
     * ================================================================ */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        logger.info("Fetching list of all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }
    /* ================================================================
     * METHOD: getUserByName
     * DESCRIPTION:
     * Searches for a user by their full name using a query parameter.
     * ================================================================ */
    @GetMapping("/search")
    public ResponseEntity<UserResponse> getUserByName(@RequestParam String name) {
        logger.info("Searching for user with name: {}", name);
        return ResponseEntity.ok(userService.getUserByName(name));
    }

    /* ================================================================
     * METHOD: updateUser
     * DESCRIPTION:
     * Updates an existing user's profile information based on their ID.
     * ================================================================ */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id, @Valid @RequestBody UserRequest request) {
        logger.info("Updating profile for user ID: {}", id);
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    /* ================================================================
     * METHOD: deleteUser
     * DESCRIPTION:
     * Removes a user profile from the database permanently.
     * ================================================================ */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        logger.info("Deleting user ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }
    /* ================================================================
     * METHOD: createUserFromAuth
     * DESCRIPTION:
     * Internal endpoint used by the Auth Service to synchronize 
     * user profiles upon registration.
     * ================================================================ */
    @Hidden
    @PostMapping("/internal")
    public ResponseEntity<UserResponse> createUserFromAuth(@RequestBody UserDto dto) {
        logger.info("Internal request: Synchronizing profile for authenticated user: {}", dto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUserFromAuth(dto));
    }
}