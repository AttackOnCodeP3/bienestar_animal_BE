package com.project.demo.rest.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.demo.logic.entity.interest.InterestRepository;
import com.project.demo.logic.entity.municipality.MunicipalityRepository;
import com.project.demo.logic.entity.neighborhood.NeighborhoodRepository;
import com.project.demo.logic.entity.rol.RoleRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserRestControllerGetByCedulaTest {

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
    @WithMockUser(roles = {"CENSISTA_USER"})
    void getByCedula_found_returns200() throws Exception {
        var u = User.builder().id(1L).name("Laura").lastname("Q").email("l@q.com").build();
        when(userRepository.findByIdentificationCard(anyString())).thenReturn(Optional.of(u));

        mockMvc.perform(get("/users/cedula/{cedula}", "1-1111-1111"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Usuario encontrado")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("l@q.com")));
    }
}
