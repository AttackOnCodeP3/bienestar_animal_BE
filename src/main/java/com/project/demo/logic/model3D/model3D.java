package com.project.demo.logic.model3D;

import java.security.Timestamp;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Represents a 3D model entity associated with an animal, including its generation state,
 * model URL, and timestamps for creation and updates.
 * This entity maps to the "model_3D_animal" table in the database and maintains relationships
 * with the Animal and StateGeneration entities.
 *
 * @author nav
 */
@Table(name = "model_3D_animal")
@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Builder
@AllArgsConstructor

public class Model3D {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url_modelo", length = 255)
    private String urlModelo;

    @Column(name = "fecha_generacion")
    private Timestamp fechaGeneracion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", referencedColumnName = "id", nullable = false)
    private Animal animal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_generation_id", referencedColumnName = "id", nullable = false)
    private StateGeneration stateGeneration;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
 
}
