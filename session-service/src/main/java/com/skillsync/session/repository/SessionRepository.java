package com.skillsync.session.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.skillsync.session.entity.Session;

/*
 * ================================================================
 * AUTHOR: Kurapati Sai Amulya
 * CLASS: SessionRepository
 * DESCRIPTION:
 * Repository interface for managing session database operations, 
 * including session presence checks.
 * ================================================================
 */
@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    boolean existsByMentorIdAndLearnerIdAndSessionTime(Long mentorId, Long learnerId, String sessionTime);
}