package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.services.ZoneService;
import fr.epsi.mspr2.infrastructure.dto.MesureResponseDTO;
import fr.epsi.mspr2.infrastructure.mapper.ZoneMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ZoneControlleurTest {

    private MockMvc mockMvc;
    private ZoneService zoneService;
    private ZoneMapper zoneMapper;

    @BeforeEach
    void setUp() {
        zoneService = mock(ZoneService.class);
        zoneMapper = mock(ZoneMapper.class);
        ZoneControlleur controller = new ZoneControlleur(zoneService, zoneMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getMesuresByZoneId_shouldReturn200WithList() throws Exception {
        UUID zoneId = UUID.randomUUID();
        Mesure m = new Mesure();
        MesureResponseDTO dto = new MesureResponseDTO();
        dto.temperature = new BigDecimal("22.0");

        when(zoneService.findMesuresByZoneId(any(), any(), any())).thenReturn(Optional.of(List.of(m)));
        when(zoneMapper.toMesureResponseDto(m)).thenReturn(dto);

        mockMvc.perform(get("/api/zone/readings/{id}", zoneId)
                        .param("startDate", "2026-01-01T00:00:00")
                        .param("endDate", "2026-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].temperature").value(22.0));
    }

    @Test
    void getMesuresByZoneId_shouldReturn200WithEmptyListWhenNoMesures() throws Exception {
        UUID zoneId = UUID.randomUUID();
        when(zoneService.findMesuresByZoneId(any(), any(), any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/zone/readings/{id}", zoneId)
                        .param("startDate", "2026-01-01T00:00:00")
                        .param("endDate", "2026-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getMesuresByZoneId_shouldReturn400WhenIllegalArgument() throws Exception {
        UUID zoneId = UUID.randomUUID();
        when(zoneService.findMesuresByZoneId(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("endDate doit être supérieur"));

        mockMvc.perform(get("/api/zone/readings/{id}", zoneId)
                        .param("startDate", "2026-01-01T00:00:00")
                        .param("endDate", "2026-12-31T23:59:59"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMesuresByZoneId_shouldReturn404WhenIllegalState() throws Exception {
        UUID zoneId = UUID.randomUUID();
        when(zoneService.findMesuresByZoneId(any(), any(), any()))
                .thenThrow(new IllegalStateException("Zone non trouvée"));

        mockMvc.perform(get("/api/zone/readings/{id}", zoneId)
                        .param("startDate", "2026-01-01T00:00:00")
                        .param("endDate", "2026-12-31T23:59:59"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLastMesuresByZoneId_shouldReturn200WhenFound() throws Exception {
        UUID zoneId = UUID.randomUUID();
        Mesure m = new Mesure();
        MesureResponseDTO dto = new MesureResponseDTO();

        when(zoneService.findLastMesuresByZoneId(zoneId)).thenReturn(Optional.of(m));
        when(zoneMapper.toMesureResponseDto(m)).thenReturn(dto);

        mockMvc.perform(get("/api/zone/{id}/readings/latest", zoneId))
                .andExpect(status().isOk());
    }

    @Test
    void getLastMesuresByZoneId_shouldReturn404WhenNotFound() throws Exception {
        UUID zoneId = UUID.randomUUID();
        when(zoneService.findLastMesuresByZoneId(zoneId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/zone/{id}/readings/latest", zoneId))
                .andExpect(status().isNotFound());
    }
}

