package com.skillsync.mentor.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.skillsync.mentor.dto.MentorDTO;
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

    @GetMapping("/user/{id}")
    public UserDTO getUser(@PathVariable Long id) {
        return mentorService.getUserDetails(id);
    }

    @PostMapping("/apply")
    public Mentor applyMentor(@RequestBody Mentor mentor) {
        return mentorService.applyMentor(mentor);
    }

    // ✅ UPDATED - returns MentorDTO with name
    @GetMapping
    public List<MentorDTO> getAllMentors() {
        return mentorService.getAllMentors();
    }

    // ✅ UPDATED - returns MentorDTO with name
    @GetMapping("/{id}")
    public MentorDTO getMentorById(@PathVariable Long id) {
        return mentorService.getMentorById(id);
    }

    @PutMapping("/{id}/availability")
    public Mentor updateAvailability(
            @PathVariable Long id,
            @RequestParam String availability) {
        return mentorService.updateAvailability(id, availability);
    }

    @GetMapping("/search")
    public Mentor getMentorByName(@RequestParam String name) {
        return mentorService.getMentorByName(name);
    }
}