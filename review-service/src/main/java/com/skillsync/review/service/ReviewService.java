package com.skillsync.review.service;

import com.skillsync.review.client.MentorClient;
import com.skillsync.review.dto.MentorDTO;
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
    private final MentorClient mentorClient;

    public ReviewService(ReviewRepository reviewRepository, MentorClient mentorClient) {
        this.reviewRepository = reviewRepository;
        this.mentorClient = mentorClient;
    }

    public ReviewResponse submitReview(ReviewRequest request) {

        // ✅ If mentorName provided, auto-resolve mentorId
        if (request.getMentorName() != null && !request.getMentorName().isEmpty()) {
            MentorDTO mentor = mentorClient.getMentorByName(request.getMentorName());
            if (mentor == null) {
                throw new RuntimeException("Mentor not found with name: " + request.getMentorName());
            }
            request.setMentorId(mentor.getId());
        } else {
            // fallback: verify mentorId directly
            MentorDTO mentor = mentorClient.getMentorById(request.getMentorId());
            if (mentor == null) {
                throw new RuntimeException("Mentor not found with id: " + request.getMentorId());
            }
        }

        boolean exists = reviewRepository.existsByLearnerIdAndMentorIdAndSessionId(
                request.getLearnerId(),
                request.getMentorId(),
                request.getSessionId()
        );
        if (exists) {
            throw new RuntimeException("You have already reviewed this session!");
        }

        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new RuntimeException("Rating must be between 1 and 5!");
        }

        Review review = new Review();
        review.setMentorId(request.getMentorId());
        review.setLearnerId(request.getLearnerId());
        review.setSessionId(request.getSessionId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review saved = reviewRepository.save(review);
        return mapToResponse(saved);
    }

    public List<ReviewResponse> getReviewsByMentor(Long mentorId) {
        return reviewRepository.findByMentorId(mentorId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Double getAverageRating(Long mentorId) {
        Double avg = reviewRepository.findAverageRatingByMentorId(mentorId);
        return avg != null ? avg : 0.0;
    }

    public List<ReviewResponse> getReviewsByLearner(Long learnerId) {
        return reviewRepository.findByLearnerId(learnerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new RuntimeException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }

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