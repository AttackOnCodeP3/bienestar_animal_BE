package com.project.demo.logic.entity.user;
import com.project.demo.logic.entity.interest.Interest;
import com.project.demo.logic.entity.municipality.Municipality;
import com.project.demo.logic.entity.neighborhood.Neighborhood;
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
import java.time.LocalDateTime;
import java.util.*;

@Table(name = "user")
@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identification_card", unique = true)
    private String identificationCard;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String lastname;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "is_nursery_home", nullable = false)
    private boolean nurseryHome = false;

    @Column(name = "temporary_password")
    private String temporaryPassword;

    @Column(name = "requires_password_change", nullable = false)
    private boolean requiresPasswordChange;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private String password;

    @Column(name = "registered_by_census_taker", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean registeredByCensusTaker = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "municipality_id")
    private Municipality municipality;

    @ManyToMany
    @JoinTable(
            name = "user_interest",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "interest_id")
    )
    private Set<Interest> interests;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "neighborhood_id")
    private Neighborhood neighborhood;

    @Column(name = "is_social_login_completed", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean socialLoginCompleted = false;

    @Column(name = "used_social_login", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean usedSocialLogin = false;

    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .toList();
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    /**
     * Indicates whether the user's account has not expired.
     * If this method returns false, Spring Security will prevent login
     * and throw an AccountExpiredException.
     *
     * @return true if the account is valid (not expired), false otherwise
     * @author dgutierrez
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user's account is not locked.
     * If this method returns false, Spring Security will prevent login
     * and throw a LockedException.
     *
     * @return true if the account is not locked, false otherwise
     * @author dgutierrez
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials (password) are not expired.
     * If this method returns false, Spring Security will prevent login
     * and throw a CredentialsExpiredException.
     *
     * @return true if the credentials are valid (not expired), false otherwise
     * @author dgutierrez
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user's account is enabled.
     * If this method returns false, Spring Security will prevent login
     * and throw a DisabledException.
     *
     * @return true if the account is active/enabled, false otherwise
     * @author dgutierrez
     */
    @Override
    public boolean isEnabled() {
        return active;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }
}
