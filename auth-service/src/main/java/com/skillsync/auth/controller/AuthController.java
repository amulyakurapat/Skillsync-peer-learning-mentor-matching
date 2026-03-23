package com.skillsync.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.skillsync.auth.dto.LoginRequest;
import com.skillsync.auth.dto.LoginResponse;
import com.skillsync.auth.dto.RegisterRequest;
import com.skillsync.auth.entity.User;
import com.skillsync.auth.security.JwtUtil;
import com.skillsync.auth.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public User registerUser(@RequestBody RegisterRequest request) {

        return authService.registerUser(
                request.getName(),
                request.getEmail(),
                request.getPassword()
        );
    }
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        User user = authService.loginUser(
                request.getEmail(),
                request.getPassword()
        );

        String token = jwtUtil.generateToken(user.getEmail());   // generate JWT

        return new LoginResponse(token);   // return token instead of User
    }
    }
