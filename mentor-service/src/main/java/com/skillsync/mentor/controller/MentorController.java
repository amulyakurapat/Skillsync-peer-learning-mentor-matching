package com.skillsync.mentor.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.skillsync.mentor.dto.MentorDTO;
import com.skillsync.mentor.dto.UserDTO;
import com.skillsync.mentor.entity.Mentor;
import com.skillsync.mentor.service.MentorService;


/*
 * ================================================================
 * AUTHOR: Kurapati Sai Amulya
 * CLASS: MentorController
 * DESCRIPTION:
 * This controller manages mentor-related operations, including 
 * mentor applications, profile retrieval, searches by skill or name, 
 * and availability management.
 * ================================================================
 */
@RestController
@RequestMapping("/mentors")
public class MentorController {

    /*
     * Logger instance for tracking mentor applications and profile activities
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MentorController.class);

    private final MentorService mentorService;

    public MentorController(MentorService mentorService) {
        this.mentorService = mentorService;
    }

    /* ================================================================
     * METHOD: getUser
     * DESCRIPTION:
     * Fetches user details for a given user ID through the Mentor Service.
     * ================================================================ */
    @GetMapping("/user/{id}")
    public UserDTO getUser(@PathVariable Long id) {
        logger.info("Fetching user details for ID: {}", id);
        return mentorService.getUserDetails(id);
    }

    /* ================================================================
     * METHOD: applyMentor
     * DESCRIPTION:
     * Submits a new mentor application for review.
     * ================================================================ */
    @PostMapping("/apply")
    public Mentor applyMentor(@RequestBody Mentor mentor) {
        logger.info("New mentor application received for user ID: {}", mentor.getUserId());
        return mentorService.applyMentor(mentor);
    }

    /* ================================================================
     * METHOD: getAllMentors
     * DESCRIPTION:
     * Retrieves a list of all registered mentors in the platform.
     * ================================================================ */
    @GetMapping
    public List<MentorDTO> getAllMentors() {
        logger.info("Fetching list of all mentors");
        return mentorService.getAllMentors();
    }

    /* ================================================================
     * METHOD: getMentorById
     * DESCRIPTION:
     * Fetches details of a specific mentor by their unique ID.
     * ================================================================ */
    @GetMapping("/{id}")
    public MentorDTO getMentorById(@PathVariable Long id) {
        logger.info("Fetching mentor profile for ID: {}", id);
        return mentorService.getMentorById(id);
    }

    /* ================================================================
     * METHOD: updateAvailability
     * DESCRIPTION:
     * Updates the active status or schedule availability of a mentor.
     * ================================================================ */
    @PutMapping("/{id}/availability")
    public Mentor updateAvailability(
            @PathVariable Long id,
            @RequestParam String availability) {
        logger.info("Updating availability for mentor ID: {}", id);
        return mentorService.updateAvailability(id, availability);
    }

    /* ================================================================
     * METHOD: getMentorByName
     * DESCRIPTION:
     * Searches for a mentor by their full name.
     * ================================================================ */
    @GetMapping("/search")
    public Mentor getMentorByName(@RequestParam String name) {
        logger.info("Searching for mentor with name: {}", name);
        return mentorService.getMentorByName(name);
    }

    /* ================================================================
     * METHOD: searchMentorsBySkill
     * DESCRIPTION:
     * Returns a list of mentors who possess a specific skill.
     * ================================================================ */
    @GetMapping("/search/skill")
    public List<MentorDTO> searchMentorsBySkill(@RequestParam String skill) {
        logger.info("Searching for mentors with skill: {}", skill);
        return mentorService.searchMentorsBySkill(skill);
    }

    /* ================================================================
     * METHOD: updateMentorStatus
     * DESCRIPTION:
     * Admin/System endpoint to update a mentor's application or active status.
     * ================================================================ */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Mentor> updateMentorStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        logger.info("Updating status for mentor ID: {} to {}", id, status);
        return ResponseEntity.ok(mentorService.updateMentorStatus(id, status));
    }
}