package com.skillsync.session.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.skillsync.session.config.RabbitMQConfig;
import com.skillsync.session.entity.Session;
import com.skillsync.session.event.SessionBookedEvent;
import com.skillsync.session.repository.SessionRepository;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final RabbitTemplate rabbitTemplate;

    public SessionService(SessionRepository sessionRepository,
                          RabbitTemplate rabbitTemplate) {
        this.sessionRepository = sessionRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public Session createSession(Session session) {

        // 1. Set status and save to DB
        session.setStatus("REQUESTED");
        Session savedSession = sessionRepository.save(session);

        // 2. Build the event object
        SessionBookedEvent event = new SessionBookedEvent(
                savedSession.getId(),
                savedSession.getMentorId(),
                savedSession.getLearnerId(),
                savedSession.getSessionTime(),
                savedSession.getStatus()
        );

        // 3. Publish to RabbitMQ exchange with routing key
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SESSION_EXCHANGE,
                RabbitMQConfig.SESSION_ROUTING_KEY,
                event
        );

        return savedSession;
    }
}