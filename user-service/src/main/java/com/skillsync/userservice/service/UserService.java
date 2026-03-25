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

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
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

    public UserResponse getUserById(Long id) {
        UserProfile user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return mapToResponse(user);
    }

    public UserResponse getUserByEmail(String email) {
        UserProfile user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(0L));
        return mapToResponse(user);
    }
    public UserResponse getUserByName(String name) {
        UserProfile user = userRepository.findByName(name)
            .orElseThrow(() -> new RuntimeException("User not found with name: " + name));
        return mapToResponse(user);
    }
    public UserResponse createUserFromAuth(UserDto dto) {
        UserProfile user = new UserProfile();
        user.setId(dto.getId());       
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        return mapToResponse(userRepository.save(user));
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        UserProfile user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setName(request.getName());
        user.setBio(request.getBio());
        user.setSkills(request.getSkills());
        user.setRole(request.getRole());
        user.setAvailability(request.getAvailability());
        return mapToResponse(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

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