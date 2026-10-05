package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.api.mapper.EntrepotMapperImpl;
import fr.epsi.mspr2.domain.models.CapteurSummary;
import fr.epsi.mspr2.domain.models.Entrepot;
import fr.epsi.mspr2.domain.models.ZoneSummary;
import fr.epsi.mspr2.domain.services.EntrepotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EntrepotController.class)
@Import(EntrepotMapperImpl.class)
class EntrepotControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockBean
    EntrepotService service;

    final UUID idEntrepot = UUID.fromString("770e8400-e29b-41d4-a716-446655440011");
    final UUID idZone = UUID.fromString("11111111-1111-1111-1111-111111111111");
    final UUID idCapteur = UUID.fromString("a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1");

    @Test
    void getAllEntrepots_renvoieIdentifiantNomEtLocalisation() throws Exception {
        when(service.getAllEntrepots()).thenReturn(List.of(
                Entrepot.builder().idEntrepot(idEntrepot).nom("Entrepot Principal").localisation("Bresil").build()));

        mockMvc.perform(get("/api/entrepots"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"idEntrepot":"%s","localisation":"Bresil","nom":"Entrepot Principal"}]
                        """.formatted(idEntrepot), true));
    }

    @Test
    void getCapteurSummary_renvoieLesDernieresMesuresEtLesMoyennes() throws Exception {
        when(service.getCapteurSummaryByEntrepotId(idEntrepot)).thenReturn(List.of(
                new CapteurSummary(idZone, "A", new BigDecimal("25.00"), new BigDecimal("45.00"),
                        new BigDecimal("22.50"), new BigDecimal("42.50")),
                new CapteurSummary(UUID.fromString("22222222-2222-2222-2222-222222222222"), "B", null, null, null, null)));

        mockMvc.perform(get("/api/entrepots/{id}/capteurSummary", idEntrepot))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"zoneId":"%s","zoneName":"A","lastTemp":25.00,"lastHumidity":45.00,"avgTemp24h":22.50,"avgHumidity24h":42.50},
                         {"zoneId":"22222222-2222-2222-2222-222222222222","zoneName":"B","lastTemp":null,"lastHumidity":null,
                          "avgTemp24h":null,"avgHumidity24h":null}]
                        """.formatted(idZone), true));
    }

    @Test
    void getCapteurSummary_renvoie400QuandLeServiceRefuseLaRequete() throws Exception {
        when(service.getCapteurSummaryByEntrepotId(idEntrepot)).thenThrow(new IllegalArgumentException("refus"));

        mockMvc.perform(get("/api/entrepots/{id}/capteurSummary", idEntrepot))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void getZonesByEntrepotId_renvoieLesZonesAvecCapteurEtNombreDeLots() throws Exception {
        when(service.findZonesByEntrepotId(idEntrepot))
                .thenReturn(List.of(new ZoneSummary(idZone, "A", idEntrepot, idCapteur, 2L)));

        mockMvc.perform(get("/api/entrepots/{id}/zones", idEntrepot))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"idZone":"%s","nomZone":"A","idEntrepot":"%s","idCapteur":"%s","lotCount":2}]
                        """.formatted(idZone, idEntrepot, idCapteur), true));
    }

    @Test
    void getZonesByEntrepotId_renvoie400QuandLeServiceRefuseLaRequete() throws Exception {
        when(service.findZonesByEntrepotId(idEntrepot)).thenThrow(new IllegalArgumentException("refus"));

        mockMvc.perform(get("/api/entrepots/{id}/zones", idEntrepot))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void unIdentifiantDEntrepotMalFormeEstUneMauvaiseRequete() throws Exception {
        mockMvc.perform(get("/api/entrepots/{id}/zones", "pas-un-uuid")).andExpect(status().isBadRequest());
    }
}
