package com.skillsync.review.service;

import com.skillsync.review.dto.ReviewRequest;
import com.skillsync.review.dto.ReviewResponse;
import com.skillsync.review.entity.Review;
import com.skillsync.review.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    // Submit a review
    public ReviewResponse submitReview(ReviewRequest request) {

        // Check duplicate review
        boolean exists = reviewRepository.existsByLearnerIdAndMentorIdAndSessionId(
                request.getLearnerId(),
                request.getMentorId(),
                request.getSessionId()
        );

        if (exists) {
            throw new RuntimeException("You have already reviewed this session!");
        }

        // Validate rating range
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new RuntimeException("Rating must be between 1 and 5!");
        }

        // Save review
        Review review = new Review();
        review.setMentorId(request.getMentorId());
        review.setLearnerId(request.getLearnerId());
        review.setSessionId(request.getSessionId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review saved = reviewRepository.save(review);
        return mapToResponse(saved);
    }

    // Get all reviews for a mentor
    public List<ReviewResponse> getReviewsByMentor(Long mentorId) {
        return reviewRepository.findByMentorId(mentorId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get average rating for a mentor
    public Double getAverageRating(Long mentorId) {
        Double avg = reviewRepository.findAverageRatingByMentorId(mentorId);
        return avg != null ? avg : 0.0;
    }

    // Get all reviews by a learner
    public List<ReviewResponse> getReviewsByLearner(Long learnerId) {
        return reviewRepository.findByLearnerId(learnerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Delete a review
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new RuntimeException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }

    // Map entity to response DTO
    private ReviewResponse mapToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setMentorId(review.getMentorId());
        response.setLearnerId(review.getLearnerId());
        response.setSessionId(review.getSessionId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}