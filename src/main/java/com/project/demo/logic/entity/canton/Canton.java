package com.project.demo.logic.entity.canton;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.project.demo.logic.entity.district.District;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Table(name = "canton")
@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
public class Canton {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "canton", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("canton")
    private List<District> districts;
}
