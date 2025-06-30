package com.project.demo.logic.entity.canton;

import jakarta.persistence.*;
import lombok.*;

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
}
