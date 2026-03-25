package com.skillsync.auth.dto;

public class ValidateResponse {
    private boolean valid;
    private Long userId;
    private String email;
    private String role;

    public ValidateResponse(boolean valid, Long userId, String email, String role) {
        this.valid = valid;
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public boolean isValid() { return valid; }
    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}