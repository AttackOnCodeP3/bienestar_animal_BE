package com.project.demo.logic.entity.sanitary_control;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.demo.logic.entity.animal.Animal;
import com.project.demo.logic.entity.sanitary_control_response.SanitaryControlResponse;
import com.project.demo.logic.entity.sanitary_control_type.SanitaryControlType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity class representing a sanitary control record.
 * This class captures the details of a sanitary control applied to an animal.
 *
 * @author dgutierrez
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sanitary_control")
public class SanitaryControl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "last_application_date")
    private LocalDate lastApplicationDate;

    /**
     * Indicates the validity status of the preventive control.
     *
     * <p>This is a transient field that is derived logically. It is calculated
     * by comparing the {last application date}
     * of the control with the defined periodicity of the control type, as
     * specified in the {@code municipal_preventive_care_config} table.</p>
     *
     * <p>This field is not persisted in the database.</p>
     *
     */
    @Transient
    private boolean validityStatus;


    @Column(name = "product_used")
    private String productUsed;

    @ManyToOne
    @JoinColumn(name = "animal_id", nullable = false)
    @JsonIgnore
    private Animal animal;

    @ManyToOne
    @JoinColumn(name = "sanitary_control_type_id")
    private SanitaryControlType sanitaryControlType;

    @ManyToOne
    @JoinColumn(name = "sanitary_control_response_id", nullable = false)
    private SanitaryControlResponse sanitaryControlResponse;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
