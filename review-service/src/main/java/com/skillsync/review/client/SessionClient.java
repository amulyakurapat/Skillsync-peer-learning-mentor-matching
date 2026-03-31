package com.skillsync.review.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.skillsync.review.dto.SessionDTO;

@FeignClient(name = "session-service")
public interface SessionClient {

    @GetMapping("/sessions/{id}")
    SessionDTO getSessionById(@PathVariable("id") Long id);
}
