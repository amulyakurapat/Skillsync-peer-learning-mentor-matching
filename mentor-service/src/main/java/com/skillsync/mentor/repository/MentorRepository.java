package com.skillsync.mentor.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.skillsync.mentor.entity.Mentor;

public interface MentorRepository extends JpaRepository<Mentor, Long> {
    Optional<Mentor> findByUserId(Long userId);
    List<Mentor> findBySkillsContainingIgnoreCase(String skill);
}