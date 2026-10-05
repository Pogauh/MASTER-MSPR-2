package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.api.mapper.AlerteMapperImpl;
import fr.epsi.mspr2.domain.models.AlerteDetails;
import fr.epsi.mspr2.domain.models.TypeAlerteEnum;
import fr.epsi.mspr2.domain.services.AlerteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlerteController.class)
@Import(AlerteMapperImpl.class)
class AlerteControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockBean
    AlerteService service;

    final UUID idEntrepot = UUID.fromString("770e8400-e29b-41d4-a716-446655440011");
    final UUID idAlerte = UUID.fromString("c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1");
    final UUID idZone = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private AlerteDetails alerte() {
        return new AlerteDetails(idAlerte, TypeAlerteEnum.HAUTETEMPERATURE, "Temperature trop haute",
                LocalDateTime.of(2025, 2, 3, 10, 0), idZone);
    }

    @Test
    void getAlerte_renvoieLesAlertesDeLEntrepot() throws Exception {
        when(service.getAlerteByEntrepot(idEntrepot, null, null)).thenReturn(List.of(alerte()));

        mockMvc.perform(get("/api/alertes").param("idEntrepot", idEntrepot.toString()))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"idAlerte":"%s","typeAlerte":"HAUTETEMPERATURE","description":"Temperature trop haute",
                          "dateEmission":"2025-02-03T10:00:00","idZone":"%s"}]
                        """.formatted(idAlerte, idZone), true));
    }

    @Test
    void getAlerte_filtreParPeriode() throws Exception {
        LocalDateTime debut = LocalDateTime.of(2025, 2, 1, 0, 0);
        LocalDateTime fin = LocalDateTime.of(2025, 2, 5, 0, 0);
        when(service.getAlerteByEntrepot(idEntrepot, debut, fin)).thenReturn(List.of(alerte()));

        mockMvc.perform(get("/api/alertes").param("idEntrepot", idEntrepot.toString())
                        .param("startDate", "2025-02-01T00:00:00").param("endDate", "2025-02-05T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"idAlerte\":\"%s\"}]".formatted(idAlerte)));
    }

    @Test
    void getAlerte_sansAlerte_renvoieUneListeVide() throws Exception {
        when(service.getAlerteByEntrepot(idEntrepot, null, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/alertes").param("idEntrepot", idEntrepot.toString()))
                .andExpect(status().isOk())
                .andExpect(content().json("[]", true));
    }

    @Test
    void getAlerte_idEntrepotEstObligatoire() throws Exception {
        mockMvc.perform(get("/api/alertes")).andExpect(status().isBadRequest());
    }

    @Test
    void getAlerte_uneDateMalFormeeEstUneMauvaiseRequete() throws Exception {
        mockMvc.perform(get("/api/alertes").param("idEntrepot", idEntrepot.toString()).param("startDate", "hier"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAlerte_renvoie400QuandLeServiceRefuseLaRequete() throws Exception {
        when(service.getAlerteByEntrepot(idEntrepot, null, null)).thenThrow(new IllegalArgumentException("refus"));

        mockMvc.perform(get("/api/alertes").param("idEntrepot", idEntrepot.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }
}
