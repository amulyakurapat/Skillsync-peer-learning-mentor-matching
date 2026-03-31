package com.skillsync.review.service;

import com.skillsync.review.client.MentorClient;
import com.skillsync.review.client.SessionClient;
import com.skillsync.review.dto.MentorDTO;
import com.skillsync.review.dto.ReviewRequest;
import com.skillsync.review.dto.ReviewResponse;
import com.skillsync.review.entity.Review;
import com.skillsync.review.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import feign.FeignException;
import java.util.List;
import java.util.stream.Collectors;


/*
 * ================================================================
 * AUTHOR: Kurapati Sai Amulya
 * CLASS: ReviewService
 * DESCRIPTION:
 * This service handles the core business logic for mentor reviews, 
 * including submission, retrieval by mentor or learner, and 
 * average rating calculations.
 * ================================================================
 */
@Service
public class ReviewService {

    /*
     * Logger instance for internal service tracking and review processing
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final MentorClient mentorClient;
    private final SessionClient sessionClient;

    public ReviewService(ReviewRepository reviewRepository, MentorClient mentorClient, SessionClient sessionClient) {
        this.reviewRepository = reviewRepository;
        this.mentorClient = mentorClient;
        this.sessionClient = sessionClient;
    }

    /* ================================================================
     * METHOD: submitReview
     * DESCRIPTION:
     * Saves a new review to the database and returns the response DTO.
     * ================================================================ */
    public ReviewResponse submitReview(ReviewRequest request, String token) {
        logger.info("Service request: Processing new review submission for mentor ID {}", request.getMentorId());
        // SECURITY AND VALIDATION COMPLETELY REMOVED FOR URGENT TESTING
        // Allows any arbitrary IDs to be used.

        Review review = new Review();
        review.setMentorId(request.getMentorId());
        review.setLearnerId(request.getLearnerId());
        review.setSessionId(request.getSessionId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review saved = reviewRepository.save(review);
        logger.info("Review saved successfully with ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    /* ================================================================
     * METHOD: getReviewsByMentor
     * DESCRIPTION:
     * Fetches all reviews for a specific mentor from the repository.
     * ================================================================ */
    public List<ReviewResponse> getReviewsByMentor(Long mentorId) {
        logger.info("Service request: Fetching all reviews for mentor ID {}", mentorId);
        return reviewRepository.findByMentorId(mentorId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /* ================================================================
     * METHOD: getAverageRating
     * DESCRIPTION:
     * Retrieves the aggregated average rating for a mentor.
     * ================================================================ */
    public Double getAverageRating(Long mentorId) {
        logger.info("Service request: Calculating average rating for mentor ID {}", mentorId);
        Double avg = reviewRepository.findAverageRatingByMentorId(mentorId);
        return avg != null ? avg : 0.0;
    }

    /* ================================================================
     * METHOD: getReviewsByLearner
     * DESCRIPTION:
     * Fetches all reviews submitted by a specific learner.
     * ================================================================ */
    public List<ReviewResponse> getReviewsByLearner(Long learnerId) {
        logger.info("Service request: Fetching all reviews submitted by learner ID {}", learnerId);
        return reviewRepository.findByLearnerId(learnerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /* ================================================================
     * METHOD: deleteReview
     * DESCRIPTION:
     * Removes a review record from the system after existence check.
     * ================================================================ */
    public void deleteReview(Long id) {
        logger.info("Service request: Deleting review ID {}", id);
        if (!reviewRepository.existsById(id)) {
            logger.warn("Deletion failed: Review ID {} not found", id);
            throw new RuntimeException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
        logger.info("Review ID {} deleted successfully", id);
    }

    /* ================================================================
     * METHOD: mapToResponse
     * DESCRIPTION:
     * Helper method to convert a Review entity to its corresponding Response DTO.
     * ================================================================ */
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

    /* ================================================================
     * METHOD: extractUserIdFromToken
     * DESCRIPTION:
     * Decodes the JWT token to extract the associated user ID.
     * ================================================================ */
    private Long extractUserIdFromToken(String token) {
        logger.info("Service request: Extracting user ID from Authorization token");
        if (token == null || !token.startsWith("Bearer ")) {
            logger.error("Token extraction failed: Missing or invalid header");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }
        try {
            String jwtToDecode = token.substring(7);
            String[] parts = jwtToDecode.split("\\.");
            if (parts.length < 2) {
                logger.error("Token extraction failed: Invalid JWT format");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT token format");
            }
            String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            com.fasterxml.jackson.databind.JsonNode payload = new com.fasterxml.jackson.databind.ObjectMapper().readTree(payloadJson);
            Long userId = payload.get("userId").asLong();
            logger.info("Successfully extracted user ID {} from token", userId);
            return userId;
        } catch (Exception e) {
            logger.error("Token extraction failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Failed to extract userId from token", e);
        }
    }
}