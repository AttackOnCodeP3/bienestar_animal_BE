package com.project.demo.logic.entity.district;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.project.demo.logic.entity.canton.Canton;
import com.project.demo.logic.entity.neighborhood.Neighborhood;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
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
    @JsonIgnoreProperties("districts")
    private Canton canton;

    @OneToMany(mappedBy = "district")
    @JsonIgnoreProperties("district")
    private List<Neighborhood> neighborhoods;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
