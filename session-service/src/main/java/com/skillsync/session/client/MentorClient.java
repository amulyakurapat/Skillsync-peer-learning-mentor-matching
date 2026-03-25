package com.skillsync.session.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.skillsync.session.dto.MentorDTO;

@FeignClient(name = "mentor-service")
public interface MentorClient {

    @GetMapping("/mentors/{id}")
    MentorDTO getMentorById(@PathVariable Long id);
}