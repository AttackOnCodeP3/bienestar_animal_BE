package com.project.demo.logic.entity.vaccine_application;

import com.project.demo.logic.entity.animal.Animal;
import com.project.demo.logic.entity.vaccine.Vaccine;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vaccine_application")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaccineApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "animal_id")
    private Animal animal;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vaccine_id")
    private Vaccine vaccine;

    @Column(name = "application_date", nullable = false)
    private LocalDate applicationDate;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
