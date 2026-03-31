package com.skillsync.review.controller;

import com.skillsync.review.dto.ReviewRequest;
import com.skillsync.review.dto.ReviewResponse;
import com.skillsync.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/*
 * ================================================================
 * AUTHOR: Kurapati Sai Amulya
 * CLASS: ReviewController
 * DESCRIPTION:
 * This controller manages the API endpoints for submitting, retrieving, 
 * and deleting mentor reviews. It utilizes the Review Service to 
 * handle the business logic and session validation.
 * ================================================================
 */
@RestController
@RequestMapping("/reviews")
@Tag(name = "Review Service", description = "APIs for managing mentor reviews")
public class ReviewController {

    /*
     * Logger instance for tracking review submissions and retrieval activities
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ReviewController.class);

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /* ================================================================
     * METHOD: submitReview
     * DESCRIPTION:
     * Endpoint to submit a new review for a mentor after a completed session.
     * ================================================================ */
    // POST /reviews — Submit a review
    @PostMapping
    @Operation(summary = "Submit a review for a mentor")
    public ResponseEntity<ReviewResponse> submitReview(@RequestBody ReviewRequest request,
                                                       @RequestHeader(value = "Authorization", required = false) String token) {
        logger.info("Submitting review for mentor ID: {} from learner ID: {}", request.getMentorId(), request.getLearnerId());
        return ResponseEntity.ok(reviewService.submitReview(request, token));
    }

    /* ================================================================
     * METHOD: getReviewsByMentor
     * DESCRIPTION:
     * Retrieves all feedback and ratings associated with a specific mentor.
     * ================================================================ */
    // GET /reviews/mentor/{mentorId} — Get all reviews for a mentor
    @GetMapping("/mentor/{mentorId}")
    @Operation(summary = "Get all reviews for a mentor")
    public ResponseEntity<List<ReviewResponse>> getReviewsByMentor(@PathVariable Long mentorId) {
        logger.info("Fetching reviews for mentor ID: {}", mentorId);
        return ResponseEntity.ok(reviewService.getReviewsByMentor(mentorId));
    }

    /* ================================================================
     * METHOD: getAverageRating
     * DESCRIPTION:
     * Calculates and returns the average star rating for a specific mentor.
     * ================================================================ */
    // GET /reviews/mentor/{mentorId}/average — Get average rating
    @GetMapping("/mentor/{mentorId}/average")
    @Operation(summary = "Get average rating of a mentor")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long mentorId) {
        logger.info("Calculating average rating for mentor ID: {}", mentorId);
        return ResponseEntity.ok(reviewService.getAverageRating(mentorId));
    }

    /* ================================================================
     * METHOD: getReviewsByLearner
     * DESCRIPTION:
     * Retrieves all reviews submitted by a specific learner across various mentors.
     * ================================================================ */
    // GET /reviews/learner/{learnerId} — Get all reviews by a learner
    @GetMapping("/learner/{learnerId}")
    @Operation(summary = "Get all reviews by a learner")
    public ResponseEntity<List<ReviewResponse>> getReviewsByLearner(@PathVariable Long learnerId) {
        logger.info("Fetching reviews submitted by learner ID: {}", learnerId);
        return ResponseEntity.ok(reviewService.getReviewsByLearner(learnerId));
    }

    /* ================================================================
     * METHOD: deleteReview
     * DESCRIPTION:
     * Administrative endpoint to remove a specific review from the system.
     * ================================================================ */
    // DELETE /reviews/{id} — Delete a review (admin only)
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a review (admin only)")
    public ResponseEntity<String> deleteReview(@PathVariable Long id) {
        logger.info("Requesting deletion of review ID: {}", id);
        reviewService.deleteReview(id);
        return ResponseEntity.ok("Review deleted successfully!");
    }
}