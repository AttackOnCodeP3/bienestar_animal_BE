package com.project.demo.logic.entity.state;
import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Table;
import org.hibernate.annotations.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.project.demo.logic.entity.model3D.Model3D;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "state_generation")
@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
@JsonIgnoreProperties({"model3DAnimals"})
public class StateGeneration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false)
    private String name;

    @Column(length = 255)
    private String description;

    @OneToMany(mappedBy = "stateGeneration", fetch = FetchType.LAZY)
    private Set<Model3D> model3DAnimals;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
