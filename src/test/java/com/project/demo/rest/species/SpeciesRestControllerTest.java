package com.project.demo.rest.species;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.demo.logic.entity.species.Species;
import com.project.demo.logic.entity.species.SpeciesRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SpeciesRestController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class SpeciesRestControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private SpeciesRepository speciesRepository;

    @MockBean private com.project.demo.logic.entity.auth.JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private org.springframework.security.authentication.AuthenticationProvider authenticationProvider;
    @MockBean private com.project.demo.logic.entity.auth.CustomOAuth2UserService customOAuth2UserService;
    @MockBean private com.project.demo.logic.entity.auth.OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    @MockBean private com.project.demo.logic.entity.auth.CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Test
    @WithMockUser
    void getAllSpecies_returns200AndList() throws Exception {
        var s1 = Species.builder().id(1L).name("Canis").build();
        var s2 = Species.builder().id(2L).name("Felis").build();
        Page<Species> page = new PageImpl<>(List.of(s1, s2), PageRequest.of(0, 10), 2);

        when(speciesRepository.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/species").param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Especies obtenidas correctamente")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Canis")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Felis")));
    }

    @Test
    @WithMockUser
    void getSpeciesById_found_returns200() throws Exception {
        var s = Species.builder().id(7L).name("Testus").build();
        when(speciesRepository.findById(7L)).thenReturn(Optional.of(s));

        mockMvc.perform(get("/species/{id}", 7))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Especie obtenida correctamente")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Testus")));
    }

    @Test
    @WithMockUser
    void getSpeciesById_notFound_returns404() throws Exception {
        when(speciesRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/species/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("no fue encontrada")));
    }
}
