package com.project.demo.logic.entity.district;

import com.project.demo.logic.entity.canton.Canton;
import com.project.demo.logic.entity.neighborhood.Neighborhood;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Table(name = "district")
@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
public class District {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "canton_id", nullable = false)
    private Canton canton;

    @OneToMany(mappedBy = "district")
    private List<Neighborhood> neighborhoods;
}
