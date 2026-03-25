package com.skillsync.notification.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.skillsync.notification.event.SessionBookedEvent;

@Service
public class NotificationConsumer {

    @RabbitListener(queues = "session.queue")
    public void handleSessionBooked(SessionBookedEvent event) {

        System.out.println("===========================================");
        System.out.println("NEW SESSION BOOKED NOTIFICATION RECEIVED!");
        System.out.println("Session ID  : " + event.getSessionId());
        System.out.println("Mentor ID   : " + event.getMentorId());
        System.out.println("Learner ID  : " + event.getLearnerId());
        System.out.println("Session Time: " + event.getSessionTime());
        System.out.println("Status      : " + event.getStatus());
        System.out.println("===========================================");
    }
}