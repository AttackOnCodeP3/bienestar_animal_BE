package com.project.demo.logic.entity.user;

import com.project.demo.logic.entity.rol.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>  {
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE %?1%")
    List<User> findUsersWithCharacterInName(String character);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = ?1")
    List<User> findAllByRoleName(RoleEnum roleName);

    @Query("SELECT u FROM User u WHERE u.name = ?1")
    Optional<User> findByName(String name);

    Optional<User> findByLastname(String lastname);

    Optional<User> findByEmail(String email);

    @Query("""
    SELECT u FROM User u
    WHERE :roleId NOT IN (
        SELECT r.id FROM u.roles r
    )
""")
    Page<User> findAllExcludingUsersWithRoleId(@Param("roleId") Long roleId, Pageable pageable);
}
