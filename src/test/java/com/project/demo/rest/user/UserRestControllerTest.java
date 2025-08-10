package com.project.demo.rest.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.demo.logic.entity.interest.InterestRepository;
import com.project.demo.logic.entity.municipality.MunicipalityRepository;
import com.project.demo.logic.entity.neighborhood.NeighborhoodRepository;
import com.project.demo.logic.entity.rol.Role;
import com.project.demo.logic.entity.rol.RoleEnum;
import com.project.demo.logic.entity.rol.RoleRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserRestControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private UserRepository userRepository;
    @MockBean private RoleRepository roleRepository;
    @MockBean private PasswordEncoder passwordEncoder;
    @MockBean private InterestRepository interestRepository;
    @MockBean private NeighborhoodRepository neighborhoodRepository;
    @MockBean private MunicipalityRepository municipalityRepository;

    @MockBean private com.project.demo.logic.entity.auth.JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private org.springframework.security.authentication.AuthenticationProvider authenticationProvider;
    @MockBean private com.project.demo.logic.entity.auth.CustomOAuth2UserService customOAuth2UserService;
    @MockBean private com.project.demo.logic.entity.auth.OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    @MockBean private com.project.demo.logic.entity.auth.CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void getAll_usesRoleFilterAndReturns200() throws Exception {
        Role superAdmin = new Role();
        superAdmin.setId(99L);
        superAdmin.setName(RoleEnum.SUPER_ADMIN);
        when(roleRepository.findByName(RoleEnum.SUPER_ADMIN)).thenReturn(Optional.of(superAdmin));

        var u = User.builder()
                .id(1L).name("Ana").lastname("Lopez")
                .email("a@a.com").birthDate(LocalDate.of(1990,1,1))
                .roles(Set.of(superAdmin))
                .build();

        Page<User> page = new PageImpl<>(java.util.List.of(u), PageRequest.of(0,10), 1);
        when(userRepository.findAllExcludingUsersWithRoleId(eq(99L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/users").param("page","1").param("size","10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Usuarios obtenidos correctamente")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Ana")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN","SUPER_ADMIN"})
    void getUserById_notFound_returns404() throws Exception {
        when(userRepository.findById(123L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/{id}", 123))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("no fue encontrado")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN","SUPER_ADMIN"})
    void addUser_encodesPasswordAndSaves_returns200() throws Exception {
        var user = User.builder()
                .name("Juan").lastname("Perez").email("j@p.com")
                .password("plain").build();

        when(passwordEncoder.encode("plain")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Usuario creado correctamente")));

        verify(passwordEncoder).encode("plain");
        verify(userRepository).save(argThat(u -> "ENCODED".equals(u.getPassword())));
    }
}
