package fr.epsi.mspr2.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.infrastructure.dto.MesureRequestDTO;
import fr.epsi.mspr2.infrastructure.dto.MesureResponseDTO;
import fr.epsi.mspr2.infrastructure.dto.MesureStatusResponseDTO;
import fr.epsi.mspr2.domain.services.MesureService;
import fr.epsi.mspr2.infrastructure.mapper.MesureMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MesureControllerTest {
    private MockMvc mockMvc;
    private MesureService serviceMesure;
    private MesureMapper mesureMapper;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        serviceMesure = mock(MesureService.class);
        mesureMapper = mock(MesureMapper.class);
        MesureController controller = new MesureController(serviceMesure, mesureMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createMesure_shouldReturn201WithId() throws Exception {
        MesureRequestDTO dto = new MesureRequestDTO();
        dto.temperature = new BigDecimal("25.00");
        dto.humidite = new BigDecimal("60.00");
        dto.dateMesure = LocalDateTime.now();
        dto.idCapteur = UUID.randomUUID();
        UUID createdId = UUID.randomUUID();
        MesureStatusResponseDTO response = new MesureStatusResponseDTO();
        response.idMesure = createdId;
        when(serviceMesure.createMesure(any())).thenReturn(response);

        mockMvc.perform(post("/api/mesures")
                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMesure").value(createdId.toString()))
                .andExpect(jsonPath("$.status").value(HttpStatus.CREATED.name()));

        verify(serviceMesure).createMesure(any());
    }

    @Test
    void deleteMesure_shouldReturn200WithId() throws Exception {
        UUID id = UUID.randomUUID();
        MesureStatusResponseDTO response = new MesureStatusResponseDTO();
        response.idMesure = id;
        when(serviceMesure.deleteMesure(id)).thenReturn(response);

        mockMvc.perform(delete("/api/mesures/deleteMesure/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMesure").value(id.toString()))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.name()));

        verify(serviceMesure).deleteMesure(id);
    }

    @Test
    void deleteMesure_shouldReturn404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(serviceMesure.deleteMesure(id)).thenReturn(null);

        mockMvc.perform(delete("/api/mesures/deleteMesure/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.idMesure", nullValue()))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.name()));

        verify(serviceMesure).deleteMesure(id);
    }

    @Test
    void getMesureById_shouldReturn200WhenFound() throws Exception {
        UUID id = UUID.randomUUID();
        MesureResponseDTO dto = new MesureResponseDTO();
        dto.idMesure = id;
        when(serviceMesure.getMesureById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/mesures/getMesure/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMesure").value(id.toString()));

        verify(serviceMesure).getMesureById(id);
    }

    @Test
    void getMesureById_shouldReturnNullBodyWhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(serviceMesure.getMesureById(id)).thenReturn(null);

        mockMvc.perform(get("/api/mesures/getMesure/{id}", id))
                .andExpect(status().isOk());
    }
}

