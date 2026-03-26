package com.skillsync.skill.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SkillRequest {

    @NotBlank(message = "Skill name is required")
    private String name;

    private String category;

    private String description;

	public String getName() {
		return name;
	}

	public String getCategory() {
		return category;
	}

	public String getDescription() {
		return description;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setDescription(String description) {
		this.description = description;
	}
    
}