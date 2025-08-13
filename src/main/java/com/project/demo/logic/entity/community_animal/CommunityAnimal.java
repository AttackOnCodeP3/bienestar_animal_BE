package com.project.demo.logic.entity.community_animal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.demo.logic.entity.animal.Animal;
import com.project.demo.logic.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.project.demo.logic.entity.municipality.Municipality;

import java.time.LocalDateTime;

/**
 * Entity class representing a community animal. This class is a specialization of the Animal entity
 * @author dgutierrez
 * 
 * Modify by nav, to add the getMunicipality method
 */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "community_animal")
public class CommunityAnimal extends Animal {

    @ManyToOne
    @JoinColumn(name = "owner_user_id", nullable = false)
    @JsonIgnore
    private User user;

    /**
     * Returns the municipality of the community animal via its user.
     * @return Municipality or null if not set
     */
    public Municipality getMunicipality() {
        return user != null ? user.getMunicipality() : null;
    }

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
