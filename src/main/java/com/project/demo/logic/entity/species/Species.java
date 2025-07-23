package com.project.demo.logic.entity.species;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.demo.logic.entity.race.Race;
import com.project.demo.logic.entity.vaccine.Vaccine;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entity representing a species.
 * This class is used to define the basic structure of a species entity.
 * @author dgutierrez
 */
@Table(name = "species")
@Entity
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class Species {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @Builder.Default
    @ManyToMany(mappedBy = "species")
    @JsonIgnore
    private Set<Vaccine> vaccines = new HashSet<>();

    @OneToMany(mappedBy = "species", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Race> races;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
