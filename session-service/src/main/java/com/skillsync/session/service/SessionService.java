package com.skillsync.session.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import com.skillsync.session.client.MentorClient;
import com.skillsync.session.config.RabbitMQConfig;
import com.skillsync.session.dto.MentorDTO;
import com.skillsync.session.entity.Session;
import com.skillsync.session.event.SessionBookedEvent;
import com.skillsync.session.repository.SessionRepository;


/*
 * ================================================================
 * AUTHOR: Kurapati Sai Amulya
 * CLASS: SessionService
 * DESCRIPTION:
 * This service handles the core business logic for session management, 
 * including booking validation (mentor status, duplicate checks), 
 * persistence, and status updates via RabbitMQ events.
 * ================================================================
 */
@Service
public class SessionService {

    /*
     * Logger instance for internal service tracking and session event processing
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SessionService.class);

    private final SessionRepository sessionRepository;
    private final RabbitTemplate rabbitTemplate;
    private final MentorClient mentorClient;  // ✅ added

    public SessionService(SessionRepository sessionRepository,
                          RabbitTemplate rabbitTemplate,
                          MentorClient mentorClient) {  // ✅ added
        this.sessionRepository = sessionRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.mentorClient = mentorClient;  // ✅ added
    }

    /* ================================================================
     * METHOD: createSession
     * DESCRIPTION:
     * Validates and creates a new session request, then broadcasts the event.
     * ================================================================ */
    public Session createSession(Session session) {
        logger.info("Service request: Processing new session booking for mentor ID {} and learner ID {}", session.getMentorId(), session.getLearnerId());

        // ✅ Verify mentor exists and is APPROVED
        MentorDTO mentor = mentorClient.getMentorById(session.getMentorId());
        if (mentor == null) {
            logger.warn("Booking failed: Mentor ID {} not found", session.getMentorId());
            throw new RuntimeException("Mentor not found with id: " + session.getMentorId());
        }
        if (!"APPROVED".equals(mentor.getStatus())) {
            logger.warn("Booking failed: Mentor ID {} is not APPROVED (Current: {})", session.getMentorId(), mentor.getStatus());
            throw new RuntimeException(
                "Mentor is not approved yet. Current status: " + mentor.getStatus() +
                ". Only APPROVED mentors can be booked."
            );
        }

        // ✅ Prevent duplicate booking for same mentor/learner/time
        if (sessionRepository.existsByMentorIdAndLearnerIdAndSessionTime(
                session.getMentorId(),
                session.getLearnerId(),
                session.getSessionTime()
        )) {
            logger.warn("Booking failed: Duplicate session detected for mentor {} and learner {}", session.getMentorId(), session.getLearnerId());
            throw new RuntimeException(
                "Duplicate booking: this learner already booked the same mentor for the same session time."
            );
        }

        session.setStatus("REQUESTED");
        Session savedSession = sessionRepository.save(session);
        logger.info("Session ID {} created successfully with status REQUESTED", savedSession.getId());

        SessionBookedEvent event = new SessionBookedEvent(
                savedSession.getId(),
                savedSession.getMentorId(),
                savedSession.getLearnerId(),
                savedSession.getSessionTime(),
                savedSession.getStatus()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SESSION_EXCHANGE,
                RabbitMQConfig.SESSION_ROUTING_KEY,
                event
        );
        logger.info("Session booked event published to RabbitMQ for session ID: {}", savedSession.getId());

        return savedSession;
    }

    /* ================================================================
     * METHOD: getSessionById
     * DESCRIPTION:
     * Retrieves a single session from the database or throws an exception.
     * ================================================================ */
    public Session getSessionById(Long id) {
        logger.info("Service request: Fetching session by ID {}", id);
        return sessionRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Fetch failed: Session ID {} not found", id);
                    return new RuntimeException("Session not found with id: " + id);
                });
    }

    /* ================================================================
     * METHOD: getAllSessions
     * DESCRIPTION:
     * Returns a list of all mentoring sessions in the system.
     * ================================================================ */
    public java.util.List<Session> getAllSessions() {
        logger.info("Service request: Fetching all recorded sessions");
        return sessionRepository.findAll();
    }

    /* ================================================================
     * METHOD: updateSessionStatus
     * DESCRIPTION:
     * Updates the session status and broadcasts the change to other services.
     * ================================================================ */
    public Session updateSessionStatus(Long id, String status) {
        logger.info("Service request: Updating status for session ID {} to {}", id, status);
        Session session = getSessionById(id);
        session.setStatus(status);
        Session savedSession = sessionRepository.save(session);
        logger.info("Session ID {} status updated to {} in database", id, status);

        // Optionally broadcast state change
        SessionBookedEvent event = new SessionBookedEvent(
                savedSession.getId(),
                savedSession.getMentorId(),
                savedSession.getLearnerId(),
                savedSession.getSessionTime(),
                savedSession.getStatus()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SESSION_EXCHANGE,
                RabbitMQConfig.SESSION_ROUTING_KEY,
                event
        );
        logger.info("Session status change event published for session ID: {}", id);

        return savedSession;
    }
}