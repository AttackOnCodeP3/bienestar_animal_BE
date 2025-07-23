package com.project.demo.logic.entity.municipality;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.demo.logic.entity.canton.Canton;
import com.project.demo.logic.entity.user.User;
import com.project.demo.rest.municipality.dto.UpdateMunicipalityRequestDTO;
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

    private String logo;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private MunicipalityStatus status;

    @ManyToOne(fetch = FetchType.EAGER)
    private Canton canton;

    @Builder.Default
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

    /**
     * Updates the municipality entity from the provided DTO.
     *
     * @param dto    the DTO containing the new values for the municipality
     * @param status the new status of the municipality
     * @param canton the canton to which the municipality belongs
     * @author dgutierrez
     */
    public void updateFromDto(UpdateMunicipalityRequestDTO dto, MunicipalityStatus status, Canton canton) {
        this.name = dto.getName();
        this.address = dto.getAddress();
        this.phone = dto.getPhone();
        this.email = dto.getEmail();
        this.status = status;
        this.canton = canton;
        this.responsibleName = dto.getResponsibleName();
        this.responsibleRole = dto.getResponsibleRole();
    }
}
