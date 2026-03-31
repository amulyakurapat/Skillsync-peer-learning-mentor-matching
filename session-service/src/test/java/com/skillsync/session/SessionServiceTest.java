package com.skillsync.session;

import com.skillsync.session.client.MentorClient;
import com.skillsync.session.config.RabbitMQConfig;
import com.skillsync.session.dto.MentorDTO;
import com.skillsync.session.entity.Session;
import com.skillsync.session.event.SessionBookedEvent;
import com.skillsync.session.repository.SessionRepository;
import com.skillsync.session.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private MentorClient mentorClient;

    @InjectMocks
    private SessionService sessionService;

    private Session mockSession;
    private MentorDTO mockMentor;

    @BeforeEach
    void setUp() {
        mockSession = new Session();
        mockSession.setId(1L);
        mockSession.setMentorId(1L);
        mockSession.setLearnerId(22L);
        mockSession.setSessionTime("2026-03-26 10:00 AM");
        mockSession.setStatus("REQUESTED");

        mockMentor = new MentorDTO();
        mockMentor.setId(1L);
        mockMentor.setStatus("APPROVED");
    }

    // ===================== CREATE SESSION TESTS =====================

    @Test
    void createSession_Success() {
        when(mentorClient.getMentorById(1L)).thenReturn(mockMentor);
        when(sessionRepository.existsByMentorIdAndLearnerIdAndSessionTime(anyLong(), anyLong(), anyString()))
                .thenReturn(false);
        when(sessionRepository.save(any(Session.class))).thenReturn(mockSession);
        doNothing().when(rabbitTemplate).convertAndSend(
                anyString(), anyString(), any(SessionBookedEvent.class)
        );

        Session result = sessionService.createSession(mockSession);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("REQUESTED", result.getStatus());
        assertEquals(1L, result.getMentorId());
        assertEquals(22L, result.getLearnerId());
    }

    @Test
    void createSession_SetsStatusToRequested() {
        Session session = new Session();
        session.setMentorId(1L);
        session.setLearnerId(22L);
        session.setSessionTime("2026-03-26 10:00 AM");
        // status NOT set intentionally

        when(mentorClient.getMentorById(1L)).thenReturn(mockMentor);
        when(sessionRepository.existsByMentorIdAndLearnerIdAndSessionTime(anyLong(), anyLong(), anyString()))
                .thenReturn(false);
        when(sessionRepository.save(any(Session.class))).thenReturn(mockSession);
        doNothing().when(rabbitTemplate).convertAndSend(
                anyString(), anyString(), any(SessionBookedEvent.class)
        );

        sessionService.createSession(session);

        // verify status was set to REQUESTED before save
        assertEquals("REQUESTED", session.getStatus());
    }

    @Test
    void createSession_SavesSessionToDatabase() {
        when(mentorClient.getMentorById(1L)).thenReturn(mockMentor);
        when(sessionRepository.existsByMentorIdAndLearnerIdAndSessionTime(anyLong(), anyLong(), anyString()))
                .thenReturn(false);
        when(sessionRepository.save(any(Session.class))).thenReturn(mockSession);
        doNothing().when(rabbitTemplate).convertAndSend(
                anyString(), anyString(), any(SessionBookedEvent.class)
        );

        sessionService.createSession(mockSession);

        verify(sessionRepository, times(1)).save(any(Session.class));
    }

    @Test
    void createSession_PublishesEventToRabbitMQ() {
        when(mentorClient.getMentorById(1L)).thenReturn(mockMentor);
        when(sessionRepository.existsByMentorIdAndLearnerIdAndSessionTime(anyLong(), anyLong(), anyString()))
                .thenReturn(false);
        when(sessionRepository.save(any(Session.class))).thenReturn(mockSession);
        doNothing().when(rabbitTemplate).convertAndSend(
                anyString(), anyString(), any(SessionBookedEvent.class)
        );

        sessionService.createSession(mockSession);

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.SESSION_EXCHANGE),
                eq(RabbitMQConfig.SESSION_ROUTING_KEY),
                any(SessionBookedEvent.class)
        );
    }

    @Test
    void createSession_ThrowsException_WhenSaveFails() {
        when(mentorClient.getMentorById(1L)).thenReturn(mockMentor);
        when(sessionRepository.existsByMentorIdAndLearnerIdAndSessionTime(anyLong(), anyLong(), anyString()))
                .thenReturn(false);
        when(sessionRepository.save(any(Session.class)))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () ->
            sessionService.createSession(mockSession)
        );
        verify(rabbitTemplate, never()).convertAndSend(
                any(String.class), any(String.class), any(Object.class)
        );
    }

    @Test
    void createSession_ThrowsException_WhenRabbitMQFails() {
        when(mentorClient.getMentorById(1L)).thenReturn(mockMentor);
        when(sessionRepository.existsByMentorIdAndLearnerIdAndSessionTime(anyLong(), anyLong(), anyString()))
                .thenReturn(false);
        when(sessionRepository.save(any(Session.class))).thenReturn(mockSession);
        doThrow(new RuntimeException("RabbitMQ error"))
                .when(rabbitTemplate).convertAndSend(
                        anyString(), anyString(), any(SessionBookedEvent.class)
                );

        assertThrows(RuntimeException.class, () ->
            sessionService.createSession(mockSession)
        );
    }
}