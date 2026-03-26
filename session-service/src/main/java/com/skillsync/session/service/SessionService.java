package com.skillsync.session.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import com.skillsync.session.client.MentorClient;
import com.skillsync.session.config.RabbitMQConfig;
import com.skillsync.session.dto.MentorDTO;
import com.skillsync.session.entity.Session;
import com.skillsync.session.event.SessionBookedEvent;
import com.skillsync.session.repository.SessionRepository;

@Service
public class SessionService {

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

    public Session createSession(Session session) {

        // ✅ Verify mentor exists and is APPROVED
        MentorDTO mentor = mentorClient.getMentorById(session.getMentorId());
        if (mentor == null) {
            throw new RuntimeException("Mentor not found with id: " + session.getMentorId());
        }
        if (!"APPROVED".equals(mentor.getStatus())) {
            throw new RuntimeException(
                "Mentor is not approved yet. Current status: " + mentor.getStatus() +
                ". Only APPROVED mentors can be booked."
            );
        }

        session.setStatus("REQUESTED");
        Session savedSession = sessionRepository.save(session);

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

        return savedSession;
    }
}