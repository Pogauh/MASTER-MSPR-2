package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.domain.models.CapteurSummary;
import fr.epsi.mspr2.domain.models.Entrepot;
import fr.epsi.mspr2.domain.models.ZoneResponse;
import fr.epsi.mspr2.domain.services.EntrepotService;
import fr.epsi.mspr2.infrastructure.dto.CapteurSummaryResponseDTO;
import fr.epsi.mspr2.infrastructure.dto.ZoneResponseDto;
import fr.epsi.mspr2.infrastructure.mapper.CapteurMapper;
import fr.epsi.mspr2.infrastructure.mapper.ZoneMapper;
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

class EntrepotControllerTest {

    private MockMvc mockMvc;
    private EntrepotService service;
    private CapteurMapper capteurMapper;
    private ZoneMapper zoneMapper;

    @BeforeEach
    void setUp() {
        service = mock(EntrepotService.class);
        capteurMapper = mock(CapteurMapper.class);
        zoneMapper = mock(ZoneMapper.class);
        EntrepotController controller = new EntrepotController(service, capteurMapper, zoneMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getAllEntrepots_shouldReturn200WithList() throws Exception {
        Entrepot e = new Entrepot();
        e.setIdEntrepot(UUID.randomUUID());
        e.setNom("Entrepot A");
        when(service.getAllEntrepots()).thenReturn(List.of(e));

        mockMvc.perform(get("/api/entrepots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Entrepot A"));
    }

    @Test
    void getCapteurSummary_shouldReturn200WhenValid() throws Exception {
        UUID entrepotId = UUID.randomUUID();
        CapteurSummary summary = mock(CapteurSummary.class);
        CapteurSummaryResponseDTO dto = new CapteurSummaryResponseDTO();

        when(service.getCapteurSummaryByEntrepotId(entrepotId)).thenReturn(List.of(summary));
        when(capteurMapper.toResponse(summary)).thenReturn(dto);

        mockMvc.perform(get("/api/entrepots/{id}/capteurSummary", entrepotId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getCapteurSummary_shouldReturn400WhenIllegalArgument() throws Exception {
        UUID entrepotId = UUID.randomUUID();
        when(service.getCapteurSummaryByEntrepotId(entrepotId))
                .thenThrow(new IllegalArgumentException("Entrepot ID ne peut pas être null"));

        mockMvc.perform(get("/api/entrepots/{id}/capteurSummary", entrepotId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getZonesByEntrepotId_shouldReturn200WithList() throws Exception {
        UUID entrepotId = UUID.randomUUID();
        ZoneResponse zone = mock(ZoneResponse.class);
        ZoneResponseDto dto = new ZoneResponseDto();

        when(service.findZonesByEntrepotId(entrepotId)).thenReturn(List.of(zone));
        when(zoneMapper.toDtoResponse(zone)).thenReturn(dto);

        mockMvc.perform(get("/api/entrepots/{id}/zones", entrepotId))
                .andExpect(status().isOk());
    }

    @Test
    void getZonesByEntrepotId_shouldReturn400WhenIllegalArgument() throws Exception {
        UUID entrepotId = UUID.randomUUID();
        when(service.findZonesByEntrepotId(any()))
                .thenThrow(new IllegalArgumentException("ID de zone ne peut pas être null"));

        mockMvc.perform(get("/api/entrepots/{id}/zones", entrepotId))
                .andExpect(status().isBadRequest());
    }
}

