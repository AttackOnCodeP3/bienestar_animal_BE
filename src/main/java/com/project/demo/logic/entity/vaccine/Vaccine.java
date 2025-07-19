package com.project.demo.logic.entity.vaccine;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.demo.logic.entity.species.Species;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "vaccine")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vaccine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @ManyToMany
    @JoinTable(
            name = "species_vaccine",
            joinColumns = @JoinColumn(name = "vaccine_id"),
            inverseJoinColumns = @JoinColumn(name = "species_id")
    )
    @Builder.Default
    private Set<Species> species = new HashSet<>();

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
