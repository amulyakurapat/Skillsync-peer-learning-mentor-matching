package com.skillsync.notification.consumer;

import com.skillsync.notification.client.MentorClient;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.skillsync.notification.client.UserClient;
import com.skillsync.notification.dto.MentorDTO;
import com.skillsync.notification.dto.UserDTO;
import com.skillsync.notification.event.SessionBookedEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class NotificationConsumer {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserClient userClient;

    @Autowired
    private MentorClient mentorClient;

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

        sendEmailToLearner(event);
    }

    private void sendEmailToLearner(SessionBookedEvent event) {
        try {
            UserDTO learner = userClient.getUserById(event.getLearnerId());
            String learnerEmail = learner.getEmail();
            MentorDTO mentor = mentorClient.getMentorById(event.getMentorId());

            String mentorName = (mentor != null && mentor.getName() != null && !mentor.getName().isBlank())
                    ? mentor.getName()
                    : "your mentor";
            String mentorSkills = (mentor != null && mentor.getSkills() != null && !mentor.getSkills().isBlank())
                    ? mentor.getSkills()
                    : "Not specified";
            String readableTime = formatSessionTime(event.getSessionTime());
            String status = event.getStatus() != null ? event.getStatus() : "REQUESTED";

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(learnerEmail);
            message.setSubject("Session Booked with " + mentorName + " | SkillSync");
            message.setText(
                "Hi " + learner.getName() + ",\n\n" +
                "Your mentoring session has been booked successfully.\n\n" +
                "Here are your session details:\n" +
                "- Session ID: " + event.getSessionId() + "\n" +
                "- Mentor: " + mentorName + "\n" +
                "- Skills: " + mentorSkills + "\n" +
                "- Date & Time: " + readableTime + "\n" +
                "- Status: " + status + "\n\n" +
                "You can view or manage this session from your SkillSync dashboard.\n\n" +
                "Thanks for choosing SkillSync!\n" +
                "The SkillSync Team"
            );
            mailSender.send(message);
            System.out.println(" Email sent to: " + learnerEmail);
        } catch (Exception e) {
            System.out.println(" Email sending failed: " + e.getMessage());
        }
    }

    private String formatSessionTime(String rawSessionTime) {
        if (rawSessionTime == null || rawSessionTime.isBlank()) {
            return "Not available";
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(rawSessionTime);
            return dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
        } catch (Exception ignored) {
            return rawSessionTime;
        }
    }
}