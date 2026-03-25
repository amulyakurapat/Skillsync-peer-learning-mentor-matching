package com.skillsync.review.dto;

public class ReviewRequest {

    private Long mentorId;       
    private String mentorName;   
    private Long learnerId;
    private Long sessionId;
    private Integer rating;
    private String comment;

    public Long getMentorId() { return mentorId; }
    public void setMentorId(Long mentorId) { this.mentorId = mentorId; }

    public String getMentorName() { return mentorName; }   
    public void setMentorName(String mentorName) { this.mentorName = mentorName; }  

    public Long getLearnerId() { return learnerId; }
    public void setLearnerId(Long learnerId) { this.learnerId = learnerId; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}