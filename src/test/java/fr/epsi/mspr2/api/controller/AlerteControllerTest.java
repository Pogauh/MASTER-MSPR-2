package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.domain.models.AlerteDetails;
import fr.epsi.mspr2.domain.services.AlerteService;
import fr.epsi.mspr2.infrastructure.dto.AlerteDetailsResponseDto;
import fr.epsi.mspr2.infrastructure.mapper.AlerteMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AlerteControllerTest {

    private MockMvc mockMvc;
    private AlerteService alerteService;
    private AlerteMapper alerteMapper;

    @BeforeEach
    void setUp() {
        alerteService = mock(AlerteService.class);
        alerteMapper = mock(AlerteMapper.class);
        AlerteController controller = new AlerteController(alerteService, alerteMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getAlerte_shouldReturn200WithList() throws Exception {
        UUID entrepotId = UUID.randomUUID();
        AlerteDetails detail = mock(AlerteDetails.class);
        AlerteDetailsResponseDto dto = new AlerteDetailsResponseDto();

        when(alerteService.getAlerteByEntrepot(eq(entrepotId), any(), any())).thenReturn(List.of(detail));
        when(alerteMapper.toDtoResponse(detail)).thenReturn(dto);

        mockMvc.perform(get("/api/alertes")
                        .param("idEntrepot", entrepotId.toString())
                        .param("startDate", "2026-01-01T00:00:00")
                        .param("endDate", "2026-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(alerteService).getAlerteByEntrepot(eq(entrepotId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getAlerte_shouldReturn400WhenServiceThrowsIllegalArgument() throws Exception {
        UUID entrepotId = UUID.randomUUID();
        when(alerteService.getAlerteByEntrepot(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Paramètre invalide"));

        mockMvc.perform(get("/api/alertes")
                        .param("idEntrepot", entrepotId.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAlerte_shouldReturn200WithEmptyListWhenNoAlertes() throws Exception {
        UUID entrepotId = UUID.randomUUID();
        when(alerteService.getAlerteByEntrepot(any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/alertes")
                        .param("idEntrepot", entrepotId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}

