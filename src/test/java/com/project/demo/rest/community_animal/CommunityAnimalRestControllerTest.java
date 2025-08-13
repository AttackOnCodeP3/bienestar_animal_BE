package com.project.demo.rest.community_animal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.demo.logic.entity.community_animal.CommunityAnimal;
import com.project.demo.logic.entity.community_animal.CommunityAnimalService;
import com.project.demo.rest.community_animal.dto.CreateAnimalRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.project.demo.logic.entity.auth.JwtService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CommunityAnimalRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommunityAnimalRestControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private JwtService jwtService;
    @MockBean private CommunityAnimalService communityAnimalService;

    @MockBean private com.project.demo.logic.entity.auth.JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private org.springframework.security.authentication.AuthenticationProvider authenticationProvider;
    @MockBean private com.project.demo.logic.entity.auth.CustomOAuth2UserService customOAuth2UserService;
    @MockBean private com.project.demo.logic.entity.auth.OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    @MockBean private com.project.demo.logic.entity.auth.CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Test
    @WithMockUser(roles = {"COMMUNITY_USER","CENSISTA_USER"})
    void createCommunityAnimal_created201() throws Exception {
        var dto = new CreateAnimalRequestDTO();
        dto.setName("Firulais");
        dto.setBirthDate(LocalDate.of(2020,1,1));
        dto.setWeight(12.3);
        dto.setSpeciesId(1L);
        dto.setRaceId(2L);
        dto.setSexId(3L);

        when(jwtService.getTokenFromHeader("Bearer X")).thenReturn("X");
        when(jwtService.extractUsername("X")).thenReturn("user@mail.com");
        when(communityAnimalService.createCommunityAnimal(eq("user@mail.com"), any(CreateAnimalRequestDTO.class)))
                .thenReturn(CommunityAnimal.builder().id(10L).name("Firulais").build());

        mockMvc.perform(post("/community-animals")
                        .header("Authorization", "Bearer X")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("registrado exitosamente")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Firulais")));
    }

    @Test
    @WithMockUser(roles = {"COMMUNITY_USER","CENSISTA_USER"})
    void createCommunityAnimal_badRequestOnIllegalArgument() throws Exception {
        var dto = new CreateAnimalRequestDTO();
        dto.setName("Pepe");
        dto.setBirthDate(LocalDate.of(2020,1,1));
        dto.setSpeciesId(1L); dto.setRaceId(2L); dto.setSexId(3L);

        when(jwtService.getTokenFromHeader("Bearer Y")).thenReturn("Y");
        when(jwtService.extractUsername("Y")).thenReturn("user@mail.com");
        when(communityAnimalService.createCommunityAnimal(anyString(), any(CreateAnimalRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Propietario no encontrado"));

        mockMvc.perform(post("/community-animals")
                        .header("Authorization", "Bearer Y")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Propietario no encontrado")));
    }

    @Test
    @WithMockUser
    void getMyAnimals_returns200() throws Exception {
        when(jwtService.getTokenFromHeader("Bearer Z")).thenReturn("Z");
        when(jwtService.extractUsername("Z")).thenReturn("me@mail.com");

        var a1 = CommunityAnimal.builder().id(1L).name("Uno").build();
        var a2 = CommunityAnimal.builder().id(2L).name("Dos").build();
        Page<CommunityAnimal> page = new PageImpl<>(List.of(a1,a2), PageRequest.of(0,10), 2);

        when(communityAnimalService.getAnimalsByUser(eq("me@mail.com"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/community-animals/mine")
                        .param("page","1").param("size","10")
                        .header("Authorization", "Bearer Z"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("obtenidos correctamente")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Uno")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Dos")));
    }
}
