package com.project.demo.logic.entity.announcement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.demo.logic.entity.announcement_state.AnnouncementState;
import com.project.demo.logic.entity.municipality.Municipality;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an announcement in the system.
 * An announcement can be associated with multiple municipalities and has a state.
 * @author dgutierrez
 */
@Entity
@Table(name = "announcement")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToMany
    @JoinTable(
            name = "announcement_municipality",
            joinColumns = @JoinColumn(name = "announcement_id"),
            inverseJoinColumns = @JoinColumn(name = "municipality_id")
    )
    @JsonIgnore
    private List<Municipality> municipalities = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "state_id", nullable = false)
    private AnnouncementState state;
}
