package com.project.demo.logic.entity.animal;

import com.project.demo.logic.entity.canton.Canton;
import com.project.demo.logic.entity.sex.Sex;
import com.project.demo.logic.entity.species.Species;
import com.project.demo.logic.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing an abandoned animal.
 * This class is used specifically for registering animal sightings by census users,
 * with reduced required fields compared to full animal profiles.
 *
 * @author gjimenez
 */
@Entity
@Table(name = "abandoned_animal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbandonedAnimal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "species_id", nullable = false)
    private Species species;

    @ManyToOne
    @JoinColumn(name = "sex_id")
    private Sex sex;

    @Enumerated(EnumType.STRING)
    @Column(name = "estimated_age", nullable = false)
    private EstimatedAgeEnum estimatedAge;

    @Enumerated(EnumType.STRING)
    @Column(name = "physical_condition", nullable = false)
    private PhysicalConditionEnum physicalCondition;

    @Enumerated(EnumType.STRING)
    @Column(name = "behavior", nullable = false)
    private BehaviorEnum behavior;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String neighborhood;

    @Column
    private String observations;

    @Column(name = "photo_url", columnDefinition = "LONGTEXT")
    private String photoUrl;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @ManyToOne
    @JoinColumn(name = "canton_id", nullable = false)
    private Canton canton;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "is_synchronized", nullable = false)
    private boolean synchronizedFlag = false;

    @Column(name = "is_abandoned", nullable = false)
    private boolean isAbandoned = true;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;
}
