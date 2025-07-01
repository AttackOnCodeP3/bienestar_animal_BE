package com.project.demo.logic.entity.interest;

import com.project.demo.logic.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Table(name = "interest")
@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
public class Interest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @ManyToMany(mappedBy = "interests", fetch = FetchType.LAZY)
    private Set<User> users;
}
