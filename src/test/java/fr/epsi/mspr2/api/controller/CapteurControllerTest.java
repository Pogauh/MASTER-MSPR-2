package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.api.mapper.MesureMapperImpl;
import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.services.CapteurService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CapteurController.class)
@Import(MesureMapperImpl.class)
class CapteurControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockBean
    CapteurService service;

    final UUID idCapteur = UUID.fromString("a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1");

    @Test
    void getMesuresByCapteurId_renvoieLesMesuresDuCapteur() throws Exception {
        UUID idMesure = UUID.fromString("d2d2d2d2-d2d2-d2d2-d2d2-d2d2d2d2d2d2");
        when(service.getMesuresByCapteurId(idCapteur)).thenReturn(List.of(Mesure.builder()
                .idMesure(idMesure).temperature(new BigDecimal("28.00")).humidite(new BigDecimal("54.00"))
                .dateMesure(LocalDateTime.of(2025, 2, 1, 0, 0)).capteur(Capteur.builder().idCapteur(idCapteur).build()).build()));

        mockMvc.perform(get("/api/capteurs/{capteurId}/mesures", idCapteur))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"idMesure":"%s","temperature":28.00,"humidite":54.00,"dateMesure":"2025-02-01T00:00:00","idCapteur":"%s"}]
                        """.formatted(idMesure, idCapteur), true));
    }

    @Test
    void getMesuresByCapteurId_sansMesure_renvoieUneListeVide() throws Exception {
        when(service.getMesuresByCapteurId(idCapteur)).thenReturn(List.of());

        mockMvc.perform(get("/api/capteurs/{capteurId}/mesures", idCapteur))
                .andExpect(status().isOk())
                .andExpect(content().json("[]", true));
    }

    @Test
    void getMesuresByCapteurId_renvoie404QuandLeServiceSignaleUnCapteurInconnu() throws Exception {
        when(service.getMesuresByCapteurId(idCapteur)).thenThrow(new IllegalStateException("not found"));

        mockMvc.perform(get("/api/capteurs/{capteurId}/mesures", idCapteur))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    void getMesuresByCapteurId_renvoie400QuandLeServiceRefuseLaRequete() throws Exception {
        when(service.getMesuresByCapteurId(idCapteur)).thenThrow(new IllegalArgumentException("capteurId est obligatoire"));

        mockMvc.perform(get("/api/capteurs/{capteurId}/mesures", idCapteur))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }
}
