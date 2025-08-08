package com.project.demo.logic.entity.municipal_preventive_care_configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.demo.logic.entity.municipality.Municipality;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "municipal_preventive_care_configuration", uniqueConstraints = {@UniqueConstraint(columnNames = {"municipality_id", "type"})})
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MunicipalPreventiveCareConfigurationEnum type;

    @Column(nullable = false)
    private int value;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "municipality_id", nullable = false)
    @JsonIgnore
    private Municipality municipality;
}
