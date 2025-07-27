package com.project.demo.logic.announcement_state;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents the state of an announcement in the system.
 * This entity defines the possible states an announcement can have,
 * such as "Draft", "Published", or "Archived".
 * @author dgutierrez
 */
@Entity
@Table(name = "announcement_state")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_state", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;
}
