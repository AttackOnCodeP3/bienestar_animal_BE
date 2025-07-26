package com.project.demo.logic.entity.municipal_preventive_care_configuration;

import com.project.demo.logic.entity.municipality.Municipality;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "municipal_preventive_care_configuration")
@Entity
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class MunicipalPreventiveCareConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int vaccinationFrequencyMonths;

    @Column(nullable = false)
    private int dewormingFrequencyMonths;

    @Column(nullable = false)
    private int fleaFrequencyMonths;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipality_id", nullable = false)
    private Municipality municipality;
}
