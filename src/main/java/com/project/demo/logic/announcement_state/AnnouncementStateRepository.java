package com.project.demo.logic.announcement_state;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnnouncementStateRepository extends JpaRepository<AnnouncementState, Long> {
    Optional<AnnouncementState> findByName(String nameState);
}
