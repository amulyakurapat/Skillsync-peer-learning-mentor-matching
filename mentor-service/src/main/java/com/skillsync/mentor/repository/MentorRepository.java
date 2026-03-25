package com.skillsync.mentor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.skillsync.mentor.entity.Mentor;

public interface MentorRepository extends JpaRepository<Mentor, Long> {
}