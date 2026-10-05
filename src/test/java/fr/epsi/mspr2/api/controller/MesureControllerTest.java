package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.api.mapper.MesureMapperImpl;
import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.models.NouvelleMesure;
import fr.epsi.mspr2.domain.services.MesureService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MesureController.class)
@Import(MesureMapperImpl.class)
class MesureControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockBean
    MesureService service;

    final UUID idMesure = UUID.fromString("d3d3d3d3-d3d3-d3d3-d3d3-d3d3d3d3d3d3");
    final UUID idCapteur = UUID.fromString("a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1");

    private Mesure mesure() {
        return Mesure.builder().idMesure(idMesure).temperature(new BigDecimal("29.50")).humidite(new BigDecimal("56.25"))
                .dateMesure(LocalDateTime.of(2025, 2, 2, 0, 0)).capteur(Capteur.builder().idCapteur(idCapteur).build()).build();
    }

    @Test
    void createMesure_renvoie201_etLIdentifiantDeLaMesureResteNul() throws Exception {
        when(service.createMesure(any(NouvelleMesure.class))).thenReturn(mesure());

        mockMvc.perform(post("/api/mesures").contentType(MediaType.APPLICATION_JSON).content("""
                        {"temperature":29.38,"humidite":54.18,"dateMesure":"2026-03-15T15:42:44","idCapteur":"%s"}
                        """.formatted(idCapteur)))
                .andExpect(status().isCreated())
                .andExpect(content().json("{\"idMesure\":null,\"status\":\"CREATED\"}", true));

        ArgumentCaptor<NouvelleMesure> demande = ArgumentCaptor.forClass(NouvelleMesure.class);
        verify(service).createMesure(demande.capture());
        assertThat(demande.getValue().temperature()).isEqualByComparingTo("29.38");
        assertThat(demande.getValue().humidite()).isEqualByComparingTo("54.18");
        assertThat(demande.getValue().dateMesure()).isEqualTo(LocalDateTime.of(2026, 3, 15, 15, 42, 44));
        assertThat(demande.getValue().idCapteur()).isEqualTo(idCapteur);
    }

    @Test
    void createMesure_uneErreurDeValidationEstPropagee() {
        when(service.createMesure(any(NouvelleMesure.class))).thenThrow(new IllegalArgumentException("idCapteur est obligatoire"));

        assertThatThrownBy(() -> mockMvc.perform(post("/api/mesures").contentType(MediaType.APPLICATION_JSON).content("{}")))
                .isInstanceOf(ServletException.class)
                .hasMessageContaining("idCapteur est obligatoire");
    }

    @Test
    void getMesureById_renvoieLaMesure() throws Exception {
        when(service.getMesureById(idMesure)).thenReturn(Optional.of(mesure()));

        mockMvc.perform(get("/api/mesures/getMesure/{id}", idMesure))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"idMesure":"%s","temperature":29.50,"humidite":56.25,"dateMesure":"2025-02-02T00:00:00","idCapteur":"%s"}
                        """.formatted(idMesure, idCapteur), true));
    }

    @Test
    void getMesureById_inconnue_renvoie200AvecUnCorpsVide() throws Exception {
        when(service.getMesureById(idMesure)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/mesures/getMesure/{id}", idMesure))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void deleteMesure_renvoie200AvecLIdentifiant() throws Exception {
        when(service.deleteMesure(idMesure)).thenReturn(Optional.of(mesure()));

        mockMvc.perform(delete("/api/mesures/deleteMesure/{id}", idMesure))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"idMesure\":\"%s\",\"status\":\"OK\"}".formatted(idMesure), true));
    }

    @Test
    void deleteMesure_inconnue_renvoie404AvecUnCorpsNotFound() throws Exception {
        when(service.deleteMesure(idMesure)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/mesures/deleteMesure/{id}", idMesure))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"idMesure\":null,\"status\":\"NOT_FOUND\"}", true));
    }
}
