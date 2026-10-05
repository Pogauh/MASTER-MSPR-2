package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.api.mapper.MesureMapperImpl;
import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.services.ZoneService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ZoneController.class)
@Import(MesureMapperImpl.class)
class ZoneControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockBean
    ZoneService service;

    final UUID idZone = UUID.fromString("11111111-1111-1111-1111-111111111111");
    final UUID idMesure = UUID.fromString("d3d3d3d3-d3d3-d3d3-d3d3-d3d3d3d3d3d3");
    final UUID idCapteur = UUID.fromString("a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1");
    final LocalDateTime debut = LocalDateTime.of(2025, 2, 1, 12, 0);
    final LocalDateTime fin = LocalDateTime.of(2025, 2, 3, 0, 0);

    private Mesure mesure() {
        return Mesure.builder().idMesure(idMesure).temperature(new BigDecimal("29.50")).humidite(new BigDecimal("56.25"))
                .dateMesure(LocalDateTime.of(2025, 2, 2, 0, 0)).capteur(Capteur.builder().idCapteur(idCapteur).build()).build();
    }

    private String mesureJson() {
        return """
                {"idMesure":"%s","temperature":29.50,"humidite":56.25,"dateMesure":"2025-02-02T00:00:00","idCapteur":"%s"}
                """.formatted(idMesure, idCapteur);
    }

    @Test
    void getMesuresByZoneId_renvoieLesMesuresDeLaPeriode() throws Exception {
        when(service.findMesuresByZoneId(idZone, debut, fin)).thenReturn(List.of(mesure()));

        mockMvc.perform(get("/api/zone/readings/{id}", idZone)
                        .param("startDate", "2025-02-01T12:00").param("endDate", "2025-02-03T00:00"))
                .andExpect(status().isOk())
                .andExpect(content().json("[" + mesureJson() + "]", true));
    }

    @Test
    void getMesuresByZoneId_exigeLesDeuxDates() throws Exception {
        mockMvc.perform(get("/api/zone/readings/{id}", idZone)).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/zone/readings/{id}", idZone).param("startDate", "2025-02-01T12:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMesuresByZoneId_renvoie400QuandLeServiceRefuseLaPeriode() throws Exception {
        when(service.findMesuresByZoneId(idZone, fin, debut)).thenThrow(new IllegalArgumentException("période inversée"));

        mockMvc.perform(get("/api/zone/readings/{id}", idZone)
                        .param("startDate", "2025-02-03T00:00:00").param("endDate", "2025-02-01T12:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void getMesuresByZoneId_renvoie404QuandLeServiceSignaleUneZoneInconnue() throws Exception {
        when(service.findMesuresByZoneId(idZone, debut, fin)).thenThrow(new IllegalStateException("zone inconnue"));

        mockMvc.perform(get("/api/zone/readings/{id}", idZone)
                        .param("startDate", "2025-02-01T12:00:00").param("endDate", "2025-02-03T00:00:00"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    void getLastMesuresByZoneId_renvoieLaDerniereMesure() throws Exception {
        when(service.findLastMesuresByZoneId(idZone)).thenReturn(Optional.of(mesure()));

        mockMvc.perform(get("/api/zone/{id}/readings/latest", idZone))
                .andExpect(status().isOk())
                .andExpect(content().json(mesureJson(), true));
    }

    @Test
    void getLastMesuresByZoneId_sansMesure_renvoie404() throws Exception {
        when(service.findLastMesuresByZoneId(idZone)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/zone/{id}/readings/latest", idZone)).andExpect(status().isNotFound());
    }
}
