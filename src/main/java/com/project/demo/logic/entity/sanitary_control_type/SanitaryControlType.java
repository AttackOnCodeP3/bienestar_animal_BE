package com.project.demo.logic.entity.sanitary_control_type;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity class representing a sanitary control type.
 * This class is used to define different types of sanitary controls that can be applied to animals.
 *
 * @author dgutierrez
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sanitary_control_type")
public class SanitaryControlType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;
}
