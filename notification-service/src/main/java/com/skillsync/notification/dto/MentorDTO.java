package com.skillsync.notification.dto;

public class MentorDTO {
    private Long id;
    private Long userId;
    private String name;
    private String skills;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
}

