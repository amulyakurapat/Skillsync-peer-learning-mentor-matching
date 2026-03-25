package com.skillsync.mentor.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.skillsync.mentor.client.UserClient;
import com.skillsync.mentor.dto.MentorDTO;
import com.skillsync.mentor.dto.UserDTO;
import com.skillsync.mentor.entity.Mentor;
import com.skillsync.mentor.repository.MentorRepository;

@Service
public class MentorService {

    private final MentorRepository mentorRepository;
    private final UserClient userClient;

    public MentorService(MentorRepository mentorRepository, UserClient userClient) {
        this.mentorRepository = mentorRepository;
        this.userClient = userClient;
    }

    public UserDTO getUserDetails(Long id) {
        return userClient.getUserById(id);
    }

    public Mentor applyMentor(Mentor mentor) {
        UserDTO user = userClient.getUserById(mentor.getUserId());
        if (user == null) {
            throw new RuntimeException("User not found with id: " + mentor.getUserId());
        }
        mentor.setId(null); // ✅ force insert, never update
        return mentorRepository.save(mentor);
    }

    // ✅ UPDATED - now returns List<MentorDTO> with name
    public List<MentorDTO> getAllMentors() {
        return mentorRepository.findAll()
                .stream()
                .map(this::enrichWithName)
                .collect(Collectors.toList());
    }

    // ✅ UPDATED - now returns MentorDTO with name
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

    // ✅ NEW - helper to enrich mentor with name from User Service
    private MentorDTO enrichWithName(Mentor mentor) {
        MentorDTO dto = new MentorDTO();
        dto.setId(mentor.getId());
        dto.setUserId(mentor.getUserId());
        dto.setBio(mentor.getBio());
        dto.setSkills(mentor.getSkills());
        dto.setAvailability(mentor.getAvailability());

        try {
            UserDTO user = userClient.getUserById(mentor.getUserId());
            if (user != null) {
                dto.setName(user.getName());
            }
        } catch (Exception e) {
            dto.setName("Unknown");  // fallback if User Service is down
        }

        return dto;
    }
}