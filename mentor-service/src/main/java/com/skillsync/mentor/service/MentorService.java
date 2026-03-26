package com.skillsync.mentor.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.skillsync.mentor.client.SkillClient;
import com.skillsync.mentor.client.UserClient;
import com.skillsync.mentor.dto.MentorDTO;
import com.skillsync.mentor.dto.SkillDTO;
import com.skillsync.mentor.dto.UserDTO;
import com.skillsync.mentor.entity.Mentor;
import com.skillsync.mentor.entity.MentorStatus;
import com.skillsync.mentor.repository.MentorRepository;

@Service
public class MentorService {

    private final MentorRepository mentorRepository;
    private final UserClient userClient;
    private final SkillClient skillClient;

    public MentorService(MentorRepository mentorRepository,
                         UserClient userClient,
                         SkillClient skillClient) {
        this.mentorRepository = mentorRepository;
        this.userClient = userClient;
        this.skillClient = skillClient;
    }

    public UserDTO getUserDetails(Long id) {
        return userClient.getUserById(id);
    }

    public Mentor applyMentor(Mentor mentor) {
        
        UserDTO user = userClient.getUserById(mentor.getUserId());
        if (user == null) {
            throw new RuntimeException("User not found with id: " + mentor.getUserId());
        }

        
        if (mentor.getSkills() != null && !mentor.getSkills().isEmpty()) {

            List<SkillDTO> allSkills = skillClient.getAllSkills();
            List<String> availableSkillNames = allSkills.stream()
                    .map(SkillDTO::getName)
                    .collect(Collectors.toList());

            List<String> enteredSkills = Arrays.asList(mentor.getSkills().split(","));
            for (String skillName : enteredSkills) {
                String trimmed = skillName.trim();
                if (!availableSkillNames.contains(trimmed)) {
                    throw new RuntimeException(
                        "Skill '" + trimmed + "' not found. " +
                        "Available skills: " + availableSkillNames
                    );
                }
            }
        }

        mentor.setId(null);
        mentor.setStatus(MentorStatus.PENDING);  // ✅ always starts as PENDING
        return mentorRepository.save(mentor);
    }

    public List<MentorDTO> getAllMentors() {
        return mentorRepository.findAll()
                .stream()
                .map(this::enrichWithName)
                .collect(Collectors.toList());
    }

    public MentorDTO getMentorById(Long id) {
        Mentor mentor = mentorRepository.findById(id).orElse(null);
        if (mentor == null) return null;
        return enrichWithName(mentor);
    }

    public Mentor updateAvailability(Long id, String availability) {
        Mentor mentor = mentorRepository.findById(id).orElse(null);
        if (mentor != null) {
            mentor.setAvailability(availability);
            return mentorRepository.save(mentor);
        }
        return null;
    }

    public Mentor getMentorByName(String name) {
        UserDTO user = userClient.getUserByName(name);
        if (user == null) {
            throw new RuntimeException("User not found with name: " + name);
        }
        return mentorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Mentor not found for user: " + name));
    }

    private MentorDTO enrichWithName(Mentor mentor) {
        MentorDTO dto = new MentorDTO();
        dto.setId(mentor.getId());
        dto.setUserId(mentor.getUserId());
        dto.setBio(mentor.getBio());
        dto.setSkills(mentor.getSkills());
        dto.setAvailability(mentor.getAvailability());
        dto.setStatus(mentor.getStatus() != null ? mentor.getStatus().name() : "PENDING");
        try {
            UserDTO user = userClient.getUserById(mentor.getUserId());
            if (user != null) {
                dto.setName(user.getName());
            }
        } catch (Exception e) {
            dto.setName("Unknown");
        }
        return dto;
    }
    public Mentor updateMentorStatus(Long id, String status) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mentor not found with id: " + id));
        
        mentor.setStatus(MentorStatus.valueOf(status.toUpperCase()));
        return mentorRepository.save(mentor);
    }
}