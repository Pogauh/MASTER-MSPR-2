package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.domain.services.HistoriqueService;
import fr.epsi.mspr2.infrastructure.dto.HistoriqueDto;
import fr.epsi.mspr2.infrastructure.mapper.HistoriqueMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HistoriqueControllerTest {

    private MockMvc mockMvc;
    private HistoriqueService historiqueService;
    private HistoriqueMapper historiqueMapper;

    @BeforeEach
    void setUp() {
        historiqueService = mock(HistoriqueService.class);
        historiqueMapper = mock(HistoriqueMapper.class);
        HistoriqueController controller = new HistoriqueController(historiqueService, historiqueMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getAllHistoriquesForLot_shouldReturn200WithoutType() throws Exception {
        UUID lotId = UUID.randomUUID();
        Historique h = new Historique();
        HistoriqueDto dto = new HistoriqueDto();

        when(historiqueService.findAllHistoriquesForLot(lotId)).thenReturn(List.of(h));
        when(historiqueMapper.toDto(h)).thenReturn(dto);

        mockMvc.perform(get("/api/historique/allHistoriquesForLot/{id}", lotId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(historiqueService).findAllHistoriquesForLot(lotId);
    }

    @Test
    void getAllHistoriquesForLot_shouldReturn200WithValidType() throws Exception {
        UUID lotId = UUID.randomUUID();
        Historique h = new Historique();
        h.setType(TypeHistoriqueEnum.TRANSFERT);
        HistoriqueDto dto = new HistoriqueDto();

        when(historiqueService.findAllHistoriquesForLotByType(lotId, TypeHistoriqueEnum.TRANSFERT)).thenReturn(List.of(h));
        when(historiqueMapper.toDto(h)).thenReturn(dto);

        mockMvc.perform(get("/api/historique/allHistoriquesForLot/{id}", lotId)
                        .param("type", "TRANSFERT"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllHistoriquesForLot_shouldReturn400WithInvalidType() throws Exception {
        UUID lotId = UUID.randomUUID();

        mockMvc.perform(get("/api/historique/allHistoriquesForLot/{id}", lotId)
                        .param("type", "INVALID_TYPE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllHistoriquesForLot_shouldNormalizeAlertToAlerte() throws Exception {
        UUID lotId = UUID.randomUUID();
        Historique h = new Historique();
        h.setType(TypeHistoriqueEnum.ALERTE);
        HistoriqueDto dto = new HistoriqueDto();

        when(historiqueService.findAllHistoriquesForLotByType(lotId, TypeHistoriqueEnum.ALERTE)).thenReturn(List.of(h));
        when(historiqueMapper.toDto(h)).thenReturn(dto);

        mockMvc.perform(get("/api/historique/allHistoriquesForLot/{id}", lotId)
                        .param("type", "ALERT"))
                .andExpect(status().isOk());

        verify(historiqueService).findAllHistoriquesForLotByType(lotId, TypeHistoriqueEnum.ALERTE);
    }

    @Test
    void getAllHistoriquesForLot_shouldReturn200WithBlankType() throws Exception {
        UUID lotId = UUID.randomUUID();
        when(historiqueService.findAllHistoriquesForLot(lotId)).thenReturn(List.of());

        mockMvc.perform(get("/api/historique/allHistoriquesForLot/{id}", lotId)
                        .param("type", ""))
                .andExpect(status().isOk());

        verify(historiqueService).findAllHistoriquesForLot(lotId);
    }
}

