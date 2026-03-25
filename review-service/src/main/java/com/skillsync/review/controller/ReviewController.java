package com.skillsync.review.controller;

import com.skillsync.review.dto.ReviewRequest;
import com.skillsync.review.dto.ReviewResponse;
import com.skillsync.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@Tag(name = "Review Service", description = "APIs for managing mentor reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // POST /reviews — Submit a review
    @PostMapping
    @Operation(summary = "Submit a review for a mentor")
    public ResponseEntity<ReviewResponse> submitReview(@RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.submitReview(request));
    }

    // GET /reviews/mentor/{mentorId} — Get all reviews for a mentor
    @GetMapping("/mentor/{mentorId}")
    @Operation(summary = "Get all reviews for a mentor")
    public ResponseEntity<List<ReviewResponse>> getReviewsByMentor(@PathVariable Long mentorId) {
        return ResponseEntity.ok(reviewService.getReviewsByMentor(mentorId));
    }

    // GET /reviews/mentor/{mentorId}/average — Get average rating
    @GetMapping("/mentor/{mentorId}/average")
    @Operation(summary = "Get average rating of a mentor")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long mentorId) {
        return ResponseEntity.ok(reviewService.getAverageRating(mentorId));
    }

    // GET /reviews/learner/{learnerId} — Get all reviews by a learner
    @GetMapping("/learner/{learnerId}")
    @Operation(summary = "Get all reviews by a learner")
    public ResponseEntity<List<ReviewResponse>> getReviewsByLearner(@PathVariable Long learnerId) {
        return ResponseEntity.ok(reviewService.getReviewsByLearner(learnerId));
    }

    // DELETE /reviews/{id} — Delete a review (admin only)
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a review (admin only)")
    public ResponseEntity<String> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok("Review deleted successfully!");
    }
}