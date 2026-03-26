package com.skillsync.notification;

import com.skillsync.notification.consumer.NotificationConsumer;
import com.skillsync.notification.event.SessionBookedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationConsumerTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    private SessionBookedEvent mockEvent;

    @BeforeEach
    void setUp() {
        mockEvent = new SessionBookedEvent();
        mockEvent.setSessionId(1L);
        mockEvent.setMentorId(1L);
        mockEvent.setLearnerId(22L);
        mockEvent.setSessionTime("2026-03-26 10:00 AM");
        mockEvent.setStatus("REQUESTED");
    }

    // ===================== HANDLE SESSION BOOKED TESTS =====================

    @Test
    void handleSessionBooked_Success() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        notificationConsumer.handleSessionBooked(mockEvent);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void handleSessionBooked_StillSucceeds_WhenEmailFails() {
        doThrow(new RuntimeException("SMTP error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // should NOT throw — email failure is caught internally
        notificationConsumer.handleSessionBooked(mockEvent);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void handleSessionBooked_WithDifferentSessionDetails() {
        mockEvent.setSessionId(5L);
        mockEvent.setMentorId(3L);
        mockEvent.setLearnerId(28L);
        mockEvent.setStatus("SCHEDULED");

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        notificationConsumer.handleSessionBooked(mockEvent);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
