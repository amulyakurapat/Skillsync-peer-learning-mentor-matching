package com.skillsync.notification.client;

import com.skillsync.notification.dto.MentorDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "mentor-service")
public interface MentorClient {

    @GetMapping("/mentors/{id}")
    MentorDTO getMentorById(@PathVariable Long id);
}

