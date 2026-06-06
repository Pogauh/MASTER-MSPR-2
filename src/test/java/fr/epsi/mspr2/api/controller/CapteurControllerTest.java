package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.infrastructure.dto.MesureResponseDTO;
import fr.epsi.mspr2.domain.services.CapteurService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CapteurControllerTest {

    private MockMvc mockMvc;
    private CapteurService serviceCapteur;

    @BeforeEach
    void setUp() {
        serviceCapteur = mock(CapteurService.class);
        CapteurController controller = new CapteurController(serviceCapteur);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getMesuresByCapteurId_shouldReturn404WhenNotOwned() throws Exception {
        UUID capteurId = UUID.randomUUID();
        when(serviceCapteur.getMesuresByCapteurId(capteurId)).thenThrow(new IllegalStateException("not found"));

        mockMvc.perform(get("/api/capteurs/{capteurId}/mesures", capteurId))
            .andExpect(status().isNotFound());
    }

    @Test
    void getMesuresByCapteurIdAndDayRange_shouldReturn200() throws Exception {
        UUID capteurId = UUID.randomUUID();
        LocalDate start = LocalDate.of(2026, 3, 1);
        LocalDate end = LocalDate.of(2026, 3, 5);
        MesureResponseDTO dto = buildResponseDto();
        when(serviceCapteur.getMesuresByCapteurIdAndDayRange(capteurId, start, end)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/capteurs/{capteurId}/mesures/by-day", capteurId)
                .param("startDate", "2026-03-01")
                .param("endDate", "2026-03-05"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].idCapteur").value(dto.idCapteur.toString()));
    }

    private MesureResponseDTO buildResponseDto() {
        MesureResponseDTO dto = new MesureResponseDTO();
        dto.idMesure = UUID.randomUUID();
        dto.idCapteur = UUID.randomUUID();
        dto.temperature = new BigDecimal("25.00");
        dto.humidite = new BigDecimal("60.00");
        dto.dateMesure = LocalDateTime.now();
        return dto;
    }
}