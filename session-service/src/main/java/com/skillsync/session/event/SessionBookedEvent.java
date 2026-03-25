package com.skillsync.session.event;

public class SessionBookedEvent {

    private Long sessionId;
    private Long mentorId;
    private Long learnerId;
    private String sessionTime;
    private String status;

    public SessionBookedEvent() {
    }

    public SessionBookedEvent(Long sessionId, Long mentorId, Long learnerId,
                               String sessionTime, String status) {
        this.sessionId = sessionId;
        this.mentorId = mentorId;
        this.learnerId = learnerId;
        this.sessionTime = sessionTime;
        this.status = status;
    }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getMentorId() { return mentorId; }
    public void setMentorId(Long mentorId) { this.mentorId = mentorId; }

    public Long getLearnerId() { return learnerId; }
    public void setLearnerId(Long learnerId) { this.learnerId = learnerId; }

    public String getSessionTime() { return sessionTime; }
    public void setSessionTime(String sessionTime) { this.sessionTime = sessionTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}