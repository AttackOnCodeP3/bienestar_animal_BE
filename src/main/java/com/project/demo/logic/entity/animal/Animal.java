package com.project.demo.logic.entity.animal;

import com.project.demo.logic.entity.canton.Canton;
import com.project.demo.logic.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "animal")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String species;

    @Column
    private String sex;

    @Enumerated(EnumType.STRING)
    @Column(name = "estimated_age", nullable = false)
    private EstimatedAgeEnum estimatedAge;

    @Column(name = "physical_condition", nullable = false)
    private String physicalCondition;

    @Column(name = "behavior", nullable = false)
    private String behavior;

    @Column
    private String observations;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @ManyToOne
    @JoinColumn(name = "canton_id", nullable = false)
    private Canton canton;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String neighborhood;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "is_abandoned", nullable = false)
    private boolean isAbandoned;

    @Column(name = "synchronized", nullable = false)
    private boolean synchronizedFlag;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;
}
