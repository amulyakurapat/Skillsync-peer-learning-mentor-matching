package com.skillsync.mentor.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.skillsync.mentor.dto.UserDTO;
import com.skillsync.mentor.entity.Mentor;
import com.skillsync.mentor.service.MentorService;

@RestController
@RequestMapping("/mentors")
public class MentorController {

    private final MentorService mentorService;

    public MentorController(MentorService mentorService) {
        this.mentorService = mentorService;
    }

    // Apply to become mentor
    @GetMapping("/user/{id}")
    public UserDTO getUser(@PathVariable Long id) {
        return mentorService.getUserDetails(id);
    }
    @PostMapping("/apply")
    public Mentor applyMentor(@RequestBody Mentor mentor) {
        return mentorService.applyMentor(mentor);
    }

    // Get all mentors
    @GetMapping
    public List<Mentor> getAllMentors() {
        return mentorService.getAllMentors();
    }

    // Get mentor by id
    @GetMapping("/{id}")
    public Mentor getMentorById(@PathVariable Long id) {
        return mentorService.getMentorById(id);
    }
    

    // Update mentor availability
    @PutMapping("/{id}/availability")
    public Mentor updateAvailability(
            @PathVariable Long id,
            @RequestParam String availability) {

        return mentorService.updateAvailability(id, availability);
    }
}