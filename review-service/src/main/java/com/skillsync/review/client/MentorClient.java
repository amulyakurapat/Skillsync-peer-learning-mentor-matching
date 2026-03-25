package com.skillsync.review.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import com.skillsync.review.dto.MentorDTO;

@FeignClient(name = "mentor-service")
public interface MentorClient {

    @GetMapping("/mentors/{id}")
    MentorDTO getMentorById(@PathVariable Long id);

    // ✅ ADD THIS
    @GetMapping("/mentors/search")
    MentorDTO getMentorByName(@RequestParam String name);
}