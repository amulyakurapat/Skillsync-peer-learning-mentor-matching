package com.skillsync.session.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.skillsync.session.entity.Session;

public interface SessionRepository extends JpaRepository<Session, Long> {

}