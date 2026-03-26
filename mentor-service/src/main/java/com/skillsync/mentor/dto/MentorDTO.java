package com.skillsync.mentor.dto;

public class MentorDTO {
    private Long id;
    private Long userId;
    private String name;
    private String bio;
    private String skills;
    private String availability;
    private String status;  // ✅ ADDED

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }
    public String getStatus() { return status; }  // ✅ ADDED
    public void setStatus(String status) { this.status = status; }  // ✅ ADDED
}