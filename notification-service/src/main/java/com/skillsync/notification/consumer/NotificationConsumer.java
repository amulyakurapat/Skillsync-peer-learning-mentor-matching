package com.skillsync.notification.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.skillsync.notification.event.SessionBookedEvent;

@Service
public class NotificationConsumer {

    @Autowired
    private JavaMailSender mailSender;

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

        // ✅ Send email to learner
        sendEmailToLearner(event);
    }

    private void sendEmailToLearner(SessionBookedEvent event) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("learner_email@gmail.com"); // ← we'll make this dynamic next
            message.setSubject("✅ Session Confirmed - SkillSync");
            message.setText(
                "Hello!\n\n" +
                "Your session has been successfully booked on SkillSync.\n\n" +
                "📋 Session Details:\n" +
                "  Session ID  : " + event.getSessionId() + "\n" +
                "  Mentor ID   : " + event.getMentorId() + "\n" +
                "  Session Time: " + event.getSessionTime() + "\n" +
                "  Status      : " + event.getStatus() + "\n\n" +
                "Thank you for using SkillSync!\n" +
                "The SkillSync Team"
            );

            mailSender.send(message);
            System.out.println("✅ Email sent successfully!");

        } catch (Exception e) {
            System.out.println("❌ Email sending failed: " + e.getMessage());
        }
    }
}