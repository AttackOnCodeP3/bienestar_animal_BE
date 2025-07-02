package com.project.demo.logic.entity.neighborhood;

import com.project.demo.logic.entity.district.District;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "neighborhood")
@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
public class Neighborhood {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "district_id", nullable = false)
    private District district;
}
