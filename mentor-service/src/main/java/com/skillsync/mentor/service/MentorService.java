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

/*
 * ================================================================
 * AUTHOR: Kurapati Sai Amulya
 * CLASS: MentorService
 * DESCRIPTION:
 * This service handles the core business logic for mentor management,
 * including mentor applications, validation of skills via Skill Service,
 * and maintaining mentor profiles with user details.
 * ================================================================
 */
@Service
public class MentorService {

    /*
     * Logger instance for internal service tracking and application validation
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MentorService.class);

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

    /* ================================================================
     * METHOD: getUserDetails
     * DESCRIPTION:
     * Communicates with the User Service to fetch details for a specific user.
     * ================================================================ */
    public UserDTO getUserDetails(Long id) {
        logger.info("Service request: Fetching user details for ID {}", id);
        return userClient.getUserById(id);
    }

    /* ================================================================
     * METHOD: applyMentor
     * DESCRIPTION:
     * Processes a new mentor application, validates skills, sets initial
     * status to PENDING, and persists it in the database.
     * ================================================================ */
    public Mentor applyMentor(Mentor mentor) {
        logger.info("Service request: Processing new mentor application for user ID {}", mentor.getUserId());
        UserDTO user = userClient.getUserById(mentor.getUserId());
        if (user == null) {
            logger.error("Application failed: User ID {} not found", mentor.getUserId());
            throw new RuntimeException("User not found with id: " + mentor.getUserId());
        }

        if (mentor.getSkills() != null && !mentor.getSkills().isEmpty()) {
            logger.info("Validating skills for mentor application: {}", mentor.getSkills());
            List<SkillDTO> allSkills = skillClient.getAllSkills();
            List<String> availableSkillNames = allSkills.stream()
                    .map(SkillDTO::getName)
                    .collect(Collectors.toList());

            List<String> enteredSkills = Arrays.asList(mentor.getSkills().split(","));
            for (String skillName : enteredSkills) {
                String trimmed = skillName.trim();
                if (!availableSkillNames.contains(trimmed)) {
                    logger.warn("Validation failed: Skill '{}' not supported", trimmed);
                    throw new RuntimeException(
                        "Skill '" + trimmed + "' not found. " +
                        "Available skills: " + availableSkillNames
                    );
                }
            }
        }

        mentor.setId(null);
        mentor.setStatus(MentorStatus.PENDING);
        logger.info("Mentor application saved successfully for user ID {}", mentor.getUserId());
        return mentorRepository.save(mentor);
    }

    /* ================================================================
     * METHOD: getAllMentors
     * DESCRIPTION:
     * Fetches all registered mentors and enriches them with name details.
     * ================================================================ */
    public List<MentorDTO> getAllMentors() {
        logger.info("Service request: Fetching all mentors");
        return mentorRepository.findAll()
                .stream()
                .map(this::enrichWithName)
                .collect(Collectors.toList());
    }

    /* ================================================================
     * METHOD: getMentorById
     * DESCRIPTION:
     * Fetches details of a specific mentor by ID and enriches with name.
     * ================================================================ */
    public MentorDTO getMentorById(Long id) {
        logger.info("Service request: Fetching mentor by ID {}", id);
        Mentor mentor = mentorRepository.findById(id).orElse(null);
        if (mentor == null) {
            logger.warn("Fetch failed: Mentor ID {} not found", id);
            return null;
        }
        return enrichWithName(mentor);
    }

    /* ================================================================
     * METHOD: updateAvailability
     * DESCRIPTION:
     * Updates the mentor's availability text/status.
     * ================================================================ */
    public Mentor updateAvailability(Long id, String availability) {
        logger.info("Service request: Updating availability for mentor ID {}", id);
        Mentor mentor = mentorRepository.findById(id).orElse(null);
        if (mentor != null) {
            mentor.setAvailability(availability);
            return mentorRepository.save(mentor);
        }
        logger.warn("Update failed: Mentor ID {} not found", id);
        return null;
    }

    /* ================================================================
     * METHOD: getMentorByName
     * DESCRIPTION:
     * Searches for a user by name then finds the associated mentor profile.
     * ================================================================ */
    public Mentor getMentorByName(String name) {
        logger.info("Service request: Fetching mentor for user name {}", name);
        UserDTO user = userClient.getUserByName(name);
        if (user == null) {
            logger.warn("Fetch failed: User '{}' not found", name);
            throw new RuntimeException("User not found with name: " + name);
        }
        return mentorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Mentor not found for user: " + name));
    }

    /* ================================================================
     * METHOD: searchMentorsBySkill
     * DESCRIPTION:
     * Returns a list of mentors whose skill tags match the query.
     * ================================================================ */
    public List<MentorDTO> searchMentorsBySkill(String skill) {
        logger.info("Service request: Searching mentors for skill '{}'", skill);
        return mentorRepository.findBySkillsContainingIgnoreCase(skill)
                .stream()
                .map(this::enrichWithName)
                .collect(Collectors.toList());
    }

    /* ================================================================
     * METHOD: enrichWithName
     * DESCRIPTION:
     * Helper method to map Mentor entity to DTO and enrich with 
     * user name from User Service.
     * ================================================================ */
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
            logger.error("Enrichment error: Could not fetch name for user ID {}: {}", mentor.getUserId(), e.getMessage());
            dto.setName("Unknown");
        }
        return dto;
    }
    /* ================================================================
     * METHOD: updateMentorStatus
     * DESCRIPTION:
     * Admin endpoint to approve/reject a mentor's application.
     * ================================================================ */
    public Mentor updateMentorStatus(Long id, String status) {
        logger.info("Service request: Updating status for mentor ID {} to {}", id, status);
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mentor not found with id: " + id));
        
        mentor.setStatus(MentorStatus.valueOf(status.toUpperCase()));
        return mentorRepository.save(mentor);
    }
}