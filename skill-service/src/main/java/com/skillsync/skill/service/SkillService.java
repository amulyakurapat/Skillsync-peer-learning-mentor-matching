package com.skillsync.skill.service;

import com.skillsync.skill.dto.SkillRequest;
import com.skillsync.skill.dto.SkillResponse;
import com.skillsync.skill.entity.Skill;
import com.skillsync.skill.repository.SkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SkillService {

    @Autowired
    private SkillRepository skillRepository;

    // Cache all skills
    @Cacheable(value = "skills")
    public List<SkillResponse> getAllSkills() {
        System.out.println("Fetching skills from DATABASE...");
        return skillRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Cache single skill by id
    @Cacheable(value = "skill", key = "#id")
    public SkillResponse getSkillById(Long id) {
        System.out.println("Fetching skill from DATABASE for id: " + id);
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Skill not found with id: " + id));
        return mapToResponse(skill);
    }

    // Clear cache after creating
    @CacheEvict(value = "skills", allEntries = true)
    public SkillResponse createSkill(SkillRequest request) {
        if (skillRepository.existsByName(request.getName())) {
            throw new RuntimeException("Skill already exists: " + request.getName());
        }
        Skill skill = new Skill();
        skill.setName(request.getName());
        skill.setCategory(request.getCategory());
        skill.setDescription(request.getDescription());
        return mapToResponse(skillRepository.save(skill));
    }

    // Clear both caches after update
    @CacheEvict(value = {"skills", "skill"}, allEntries = true)
    public SkillResponse updateSkill(Long id, SkillRequest request) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Skill not found with id: " + id));
        skill.setName(request.getName());
        skill.setCategory(request.getCategory());
        skill.setDescription(request.getDescription());
        return mapToResponse(skillRepository.save(skill));
    }

    // Clear both caches after delete
    @CacheEvict(value = {"skills", "skill"}, allEntries = true)
    public void deleteSkill(Long id) {
        if (!skillRepository.existsById(id)) {
            throw new RuntimeException("Skill not found with id: " + id);
        }
        skillRepository.deleteById(id);
    }
    public SkillResponse getSkillByName(String name) {
        Skill skill = skillRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Skill not found: " + name));
        return mapToResponse(skill);
    }

    private SkillResponse mapToResponse(Skill skill) {
        return new SkillResponse(
                skill.getId(),
                skill.getName(),
                skill.getCategory(),
                skill.getDescription()
        );
    }
}