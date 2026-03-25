package com.skillsync.mentor.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.skillsync.mentor.client.UserClient;
import com.skillsync.mentor.dto.UserDTO;
import com.skillsync.mentor.entity.Mentor;
import com.skillsync.mentor.repository.MentorRepository;

@Service
public class MentorService {

    private final MentorRepository mentorRepository;
    private final UserClient userClient;   // Feign Client added

    // Updated constructor
    public MentorService(MentorRepository mentorRepository, UserClient userClient) {
        this.mentorRepository = mentorRepository;
        this.userClient = userClient;
    }

    // Feign method (NEW)
    public UserDTO getUserDetails(Long id) {
        return userClient.getUserById(id);
    }

    // Existing methods (UNCHANGED)

    public Mentor applyMentor(Mentor mentor) {
        return mentorRepository.save(mentor);
    }

    public List<Mentor> getAllMentors() {
        return mentorRepository.findAll();
    }

    public Mentor getMentorById(Long id) {
        return mentorRepository.findById(id).orElse(null);
    }

    public Mentor updateAvailability(Long id, String availability) {
        Mentor mentor = mentorRepository.findById(id).orElse(null);

        if (mentor != null) {
            mentor.setAvailability(availability);
            return mentorRepository.save(mentor);
        }

        return null;
    }
}