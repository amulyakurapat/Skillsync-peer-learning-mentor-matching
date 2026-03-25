package com.skillsync.auth.dto;

public class RefreshTokenResponse {
    private String accessToken;
    private String refreshToken;

    public RefreshTokenResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
}