package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.services.CapteurService;
import fr.epsi.mspr2.infrastructure.dto.MesureResponseDTO;
import fr.epsi.mspr2.infrastructure.mapper.MesureMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CapteurControllerTest {

    private MockMvc mockMvc;
    private CapteurService serviceCapteur;
    private MesureMapper mesureMapper;

    @BeforeEach
    void setUp() {
        serviceCapteur = mock(CapteurService.class);
        mesureMapper = mock(MesureMapper.class);
        CapteurController controller = new CapteurController(serviceCapteur, mesureMapper);
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
    void getMesuresByCapteurId_shouldReturn200WithList() throws Exception {
        UUID capteurId = UUID.randomUUID();
        Capteur capteur = new Capteur();
        capteur.setIdCapteur(capteurId);
        Mesure mesure = new Mesure();
        mesure.setIdMesure(UUID.randomUUID());
        mesure.setCapteur(capteur);

        MesureResponseDTO dto = new MesureResponseDTO();
        dto.idCapteur = capteurId;

        when(serviceCapteur.getMesuresByCapteurId(capteurId)).thenReturn(List.of(mesure));
        when(mesureMapper.toResponse(mesure)).thenReturn(dto);

        mockMvc.perform(get("/api/capteurs/{capteurId}/mesures", capteurId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idCapteur").value(capteurId.toString()));
    }

    @Test
    void getMesuresByCapteurId_shouldReturn400WhenIllegalArgument() throws Exception {
        UUID capteurId = UUID.randomUUID();
        when(serviceCapteur.getMesuresByCapteurId(capteurId))
                .thenThrow(new IllegalArgumentException("capteurId est obligatoire"));

        mockMvc.perform(get("/api/capteurs/{capteurId}/mesures", capteurId))
                .andExpect(status().isBadRequest());
    }

//    @Test
//    void getMesuresByZoneIdAndDayRange_shouldReturn200() throws Exception {
//        UUID capteurId = UUID.randomUUID();
//        LocalDate start = LocalDate.of(2026, 3, 1);
//        LocalDate end = LocalDate.of(2026, 3, 5);
//        MesureResponseDTO dto = buildResponseDto();
//        when(service.getMesuresByZoneIdAndDayRange(capteurId, start, end)).thenReturn(List.of(dto));
//
//        mockMvc.perform(get("/zone/readingsByDay/{zoneId}", capteurId)
//                .param("startDate", "2026-03-01")
//                .param("endDate", "2026-03-05"))
//            .andExpect(status().isOk())
//            .andExpect(jsonPath("$[0].idCapteur").value(dto.idCapteur.toString()));
//    }
//
//    private MesureResponseDTO buildResponseDto() {
//        MesureResponseDTO dto = new MesureResponseDTO();
//        dto.idMesure = UUID.randomUUID();
//        dto.idCapteur = UUID.randomUUID();
//        dto.temperature = new BigDecimal("25.00");
//        dto.humidite = new BigDecimal("60.00");
//        dto.dateMesure = LocalDateTime.now();
//        return dto;
//    }
}