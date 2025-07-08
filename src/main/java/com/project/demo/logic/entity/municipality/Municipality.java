package com.project.demo.logic.entity.municipality;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.demo.logic.entity.canton.Canton;
import com.project.demo.logic.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
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

    @Column(name = "responsible_name")
    private String responsibleName;

    @Column(name = "responsible_role")
    private String responsibleRole;

    @Column(name = "responsible_email")
    private String responsibleEmail;

    @Column()
    private String logo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MunicipalityStatusEnum status = MunicipalityStatusEnum.ACTIVE;

    @ManyToOne(fetch = FetchType.EAGER)
    private Canton canton;

    @OneToMany(mappedBy = "municipality", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<User> users = new ArrayList<>();

    @Column(name = "updated_by")
    private String updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;
}
