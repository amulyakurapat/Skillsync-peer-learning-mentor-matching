package com.skillsync.session.controller;

import org.springframework.web.bind.annotation.*;

import com.skillsync.session.entity.Session;
import com.skillsync.session.service.SessionService;

/*
 * ================================================================
 * AUTHOR: Kurapati Sai Amulya
 * CLASS: SessionController
 * DESCRIPTION:
 * This controller manages the lifecycle of mentoring sessions, including 
 * booking, status updates (accept/reject/complete/cancel), and 
 * retrieval of session details.
 * ================================================================
 */
@RestController
@RequestMapping("/sessions")
public class SessionController {

    /*
     * Logger instance for tracking session lifecycle events and booking activities
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SessionController.class);

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /* ================================================================
     * METHOD: bookSession
     * DESCRIPTION:
     * Endpoint to create a new session request between a learner and mentor.
     * ================================================================ */
    @PostMapping
    public Session bookSession(@RequestBody Session session) {
        logger.info("Booking request received for mentor ID: {} and learner ID: {}", session.getMentorId(), session.getLearnerId());
        return sessionService.createSession(session);
    }

    /* ================================================================
     * METHOD: getSession
     * DESCRIPTION:
     * Retrieves specific session details using the provided unique session ID.
     * ================================================================ */
    @GetMapping("/{id}")
    public Session getSession(@PathVariable Long id) {
        logger.info("Fetching session details for ID: {}", id);
        return sessionService.getSessionById(id);
    }

    /* ================================================================
     * METHOD: getAllSessions
     * DESCRIPTION:
     * Returns a full list of all recorded sessions in the system.
     * ================================================================ */
    @GetMapping
    public java.util.List<Session> getAllSessions() {
        logger.info("Fetching list of all mentoring sessions");
        return sessionService.getAllSessions();
    }

    /* ================================================================
     * METHOD: acceptSession
     * DESCRIPTION:
     * Endpoint for mentors to accept a pending session request.
     * ================================================================ */
    @PostMapping("/{id}/accept")
    public Session acceptSession(@PathVariable Long id) {
        logger.info("Updating status to ACCEPTED for session ID: {}", id);
        return sessionService.updateSessionStatus(id, "ACCEPTED");
    }

    /* ================================================================
     * METHOD: rejectSession
     * DESCRIPTION:
     * Endpoint for mentors to decline a pending session request.
     * ================================================================ */
    @PostMapping("/{id}/reject")
    public Session rejectSession(@PathVariable Long id) {
        logger.info("Updating status to REJECTED for session ID: {}", id);
        return sessionService.updateSessionStatus(id, "REJECTED");
    }

    /* ================================================================
     * METHOD: completeSession
     * DESCRIPTION:
     * Marks a session as successfully completed after the meeting.
     * ================================================================ */
    @PostMapping("/{id}/complete")
    public Session completeSession(@PathVariable Long id) {
        logger.info("Updating status to COMPLETED for session ID: {}", id);
        return sessionService.updateSessionStatus(id, "COMPLETED");
    }

    /* ================================================================
     * METHOD: cancelSession
     * DESCRIPTION:
     * Allows a learner or system to cancel a scheduled or pending session.
     * ================================================================ */
    @PostMapping("/{id}/cancel")
    public Session cancelSession(@PathVariable Long id) {
        logger.info("Updating status to CANCELLED for session ID: {}", id);
        return sessionService.updateSessionStatus(id, "CANCELLED");
    }
}