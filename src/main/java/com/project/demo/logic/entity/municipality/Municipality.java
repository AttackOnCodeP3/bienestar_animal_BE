package com.project.demo.logic.entity.municipality;

import com.project.demo.logic.entity.canton.Canton;
import com.project.demo.logic.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Table(name = "municipality")
@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
public class Municipality {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column()
    private String address;

    @Column()
    private String phone;

    @Column(unique = true, nullable = false)
    private String email;

    @ManyToOne(fetch = FetchType.EAGER)
    private Canton canton;

    @OneToMany(mappedBy = "municipality", cascade = CascadeType.ALL)
    private List<User> users = new ArrayList<>();
}
