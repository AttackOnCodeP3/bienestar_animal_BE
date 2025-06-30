package com.project.demo.logic.entity.user;
import com.project.demo.logic.entity.municipality.Municipality;
import com.project.demo.logic.entity.rol.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;


@Table(name = "user")
@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identification_card", unique = true, nullable = false)
    private String identificationCard;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String lastname;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "phone_number", unique = true, length = 15, nullable = false)
    private String phoneNumber;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "is_nursery_home", nullable = false)
    private boolean isNurseryHome = false;

    @Column(name = "temporary_password")
    private String temporaryPassword;

    @Column(name = "requires_password_change", nullable = false)
    private boolean requiresPasswordChange;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private String password;

    @Column(name = "registered_by_census_taker", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean registeredByCensusTaker = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "municipality_id", nullable = false)
    private Municipality municipality;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.getName().toString());
        return List.of(authority);
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false)
    private Role role;

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }


    public User setRole(Role role) {
        this.role = role;
        return this;
    }
}
