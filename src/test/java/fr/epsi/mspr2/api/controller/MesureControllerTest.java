package fr.epsi.mspr2.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.epsi.mspr2.adapters.dto.MesureRequestDTO;
import fr.epsi.mspr2.adapters.dto.MesureResponseDTO;
import fr.epsi.mspr2.adapters.dto.MesureStatusResponseDTO;
import fr.epsi.mspr2.domain.services.ServiceMesure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MesureControllerTest {
    private MockMvc mockMvc;
    private ServiceMesure serviceMesure;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        serviceMesure = mock(ServiceMesure.class);
        MesureController controller = new MesureController(serviceMesure);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createMesure_shouldReturn201WithId() throws Exception {
        MesureRequestDTO dto = new MesureRequestDTO();
        dto.temperature = 25;
        dto.humidite = 60;
        dto.dateMesure = LocalDateTime.now();
        dto.idEntrepot = UUID.randomUUID();
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
    void getAllMesures_shouldReturnList() throws Exception {
        MesureResponseDTO dto = new MesureResponseDTO();
        dto.temperature = 22;
        MesureResponseDTO dto2 = new MesureResponseDTO();
        dto2.temperature = 18;
        List<MesureResponseDTO> mesures = List.of(dto, dto2);
        when(serviceMesure.getAllMesuresForEntrepot()).thenReturn(mesures);

        mockMvc.perform(get("/api/mesures/getAllMesures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].temperature").value(22))
                .andExpect(jsonPath("$[1].temperature").value(18));

        verify(serviceMesure).getAllMesuresForEntrepot();
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
}

