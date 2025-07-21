package com.project.demo.logic.entity.animal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.project.demo.logic.entity.animal.dto.AgeDTO;
import com.project.demo.logic.entity.animal_type.AnimalType;
import com.project.demo.logic.entity.model3D.Model3D;
import com.project.demo.logic.entity.race.Race;
import com.project.demo.logic.entity.sanitary_control.SanitaryControl;
import com.project.demo.logic.entity.sex.Sex;
import com.project.demo.logic.entity.species.Species;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Entity representing an animal.
 * This class is used to define the basic structure of an animal entity.
 *
 * @author dgutierrez
 * @updatedBy gjimenez
 * @updated byb nav
 * relation with model added
 */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "animal")
@Inheritance(strategy = InheritanceType.JOINED)
@JsonIgnoreProperties({"model3DAnimals"})
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double weight;

    @ManyToOne
    @JoinColumn(name = "species_id", nullable = false)
    private Species species;

    @ManyToOne
    @JoinColumn(name = "race_id", nullable = false)
    private Race race;

    @ManyToOne
    @JoinColumn(name = "sex_id", nullable = false)
    private Sex sex;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    private Double latitude;

    private Double longitude;

    @Builder.Default
    @OneToMany(mappedBy = "animal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SanitaryControl> sanitaryControls = new ArrayList<>();

    @OneToMany(mappedBy = "animal", fetch = FetchType.LAZY)
    private Set<Model3D> model3DAnimals;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    public AgeDTO getAge() {
        if (birthDate == null) return new AgeDTO(0, 0, 0);
        Period period = Period.between(birthDate, LocalDate.now());
        return new AgeDTO(period.getYears(), period.getMonths(), period.getDays());
    }
}