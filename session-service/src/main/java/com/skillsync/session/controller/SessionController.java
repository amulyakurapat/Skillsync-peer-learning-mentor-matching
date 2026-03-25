package com.skillsync.session.controller;

import org.springframework.web.bind.annotation.*;

import com.skillsync.session.entity.Session;
import com.skillsync.session.service.SessionService;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public Session bookSession(@RequestBody Session session) {
        return sessionService.createSession(session);
    }
}