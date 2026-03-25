package com.skillsync.auth.dto;

import jakarta.validation.constraints.NotBlank;
import com.skillsync.auth.entity.RoleName;
import io.swagger.v3.oas.annotations.media.Schema;

public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @Schema(allowableValues = {"ROLE_LEARNER", "ROLE_MENTOR"})
    private RoleName role;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public RoleName getRole() { return role; }
    public void setRole(RoleName role) { this.role = role; }
}