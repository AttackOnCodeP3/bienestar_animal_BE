package com.project.demo.logic.entity.animal;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.project.demo.logic.entity.model3D.Model3D;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Table(name = "animal")
@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
@JsonIgnoreProperties({"model3DAnimals"})
public class Animal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "species_id")
    private Integer speciesId;

    @Column(name = "raza_id")
    private Integer razaId;

    @Column(name = "gender_id")
    private Integer genderId;

    @Column(name = "origen_register_id")
    private Integer origenRegisterId;

    private String name;

    @Column(name = "age_years")
    private Integer ageYears;

    private Float weight;

    @Column(name = "register_by_census_id")
    private Integer registerByCensusId;

    @Column(name = "coordenadas_gps")
    private String coordenadasGps;

    @Column(name = "date_birthday")
    @Temporal(TemporalType.DATE)
    private Date dateBirthday;

    @Column(name = "type_animal_id")
    private Integer typeAnimalId;

    @OneToMany(mappedBy = "animal", fetch = FetchType.LAZY)
    private Set<Model3D> model3DAnimals;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}