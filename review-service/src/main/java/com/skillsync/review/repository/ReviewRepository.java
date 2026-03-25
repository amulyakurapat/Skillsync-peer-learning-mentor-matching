package com.skillsync.review.repository;

import com.skillsync.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Get all reviews for a mentor
    List<Review> findByMentorId(Long mentorId);

    // Get all reviews by a learner
    List<Review> findByLearnerId(Long learnerId);

    // Check if learner already reviewed this mentor for this session
    boolean existsByLearnerIdAndMentorIdAndSessionId(
            Long learnerId, Long mentorId, Long sessionId);

    // Calculate average rating for a mentor
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.mentorId = :mentorId")
    Double findAverageRatingByMentorId(Long mentorId);
}