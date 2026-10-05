package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.api.mapper.HistoriqueMapperImpl;
import fr.epsi.mspr2.api.mapper.LotMapperImpl;
import fr.epsi.mspr2.api.mapper.MesureMapperImpl;
import fr.epsi.mspr2.domain.exceptions.LotIntrouvableException;
import fr.epsi.mspr2.domain.exceptions.TransfertLotInvalideException;
import fr.epsi.mspr2.domain.models.Entrepot;
import fr.epsi.mspr2.domain.models.Exploitation;
import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.models.StatusEnum;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.domain.models.Zone;
import fr.epsi.mspr2.domain.services.LotService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LotController.class)
@Import({LotMapperImpl.class, HistoriqueMapperImpl.class, MesureMapperImpl.class})
class LotControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockBean
    LotService service;

    final UUID idLot = UUID.fromString("f1f1f1f1-f1f1-f1f1-f1f1-f1f1f1f1f1f1");
    final UUID idZone = UUID.fromString("11111111-1111-1111-1111-111111111111");
    final UUID idEntrepot = UUID.fromString("770e8400-e29b-41d4-a716-446655440011");
    final UUID idExploitation = UUID.fromString("aa0e8400-e29b-41d4-a716-446655440022");
    final UUID idHistorique = UUID.fromString("e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1");
    Lot lot;

    @BeforeEach
    void setUp() {
        lot = Lot.builder()
                .idLot(idLot)
                .status(StatusEnum.COMPLIANT)
                .quantite(100)
                .caracteristique(2)
                .dateStockage(LocalDateTime.of(2025, 1, 10, 8, 0))
                .dateProduction(LocalDate.of(2025, 1, 1))
                .exploitation(Exploitation.builder().idExploitation(idExploitation).nom("Flamenco").build())
                .zone(Zone.builder().idZone(idZone).nom("A")
                        .entrepot(Entrepot.builder().idEntrepot(idEntrepot).nom("Principal").build()).build())
                .historique(List.of(Historique.builder()
                        .idHistorique(idHistorique)
                        .type(TypeHistoriqueEnum.TRANSFERT)
                        .dateEmission(LocalDateTime.of(2025, 1, 20, 10, 0))
                        .zonePrecedente(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                        .zoneActuelle(idZone)
                        .detail("Transfert")
                        .idLot(idLot)
                        .build()))
                .build();
    }

    private String lotJson() {
        return """
                {"idLot":"%s","status":"COMPLIANT","quantite":100,"caracteristique":2,"dateSortie":null,
                 "dateStockage":"2025-01-10T08:00:00","dateProduction":"2025-01-01","emplacement":"%s",
                 "idExploitation":"%s","idEntrepot":"%s",
                 "historique":[{"idHistorique":"%s","type":"TRANSFERT","dateEmission":"2025-01-20T10:00:00",
                                "zonePrecedente":"22222222-2222-2222-2222-222222222222","zoneActuelle":"%s","detail":"Transfert"}]}
                """.formatted(idLot, idZone, idExploitation, idEntrepot, idHistorique, idZone);
    }

    // ------------------------------------------------------------------ création
    @Test
    void createLot_renvoie201AvecLIdentifiantDuLot() throws Exception {
        when(service.createLot(any(Lot.class))).thenReturn(lot);

        mockMvc.perform(post("/api/lots").contentType(MediaType.APPLICATION_JSON).content("""
                        {"status":"COMPLIANT","quantite":120,"caracteristique":2,"dateProduction":"2026-03-15",
                         "idExploitation":"%s","idZone":"%s"}""".formatted(idExploitation, idZone)))
                .andExpect(status().isCreated())
                .andExpect(content().json("{\"idLot\":\"%s\",\"status\":\"CREATED\"}".formatted(idLot), true));

        ArgumentCaptor<Lot> demande = ArgumentCaptor.forClass(Lot.class);
        verify(service).createLot(demande.capture());
        assertThat(demande.getValue().getStatus()).isEqualTo(StatusEnum.COMPLIANT);
        assertThat(demande.getValue().getQuantite()).isEqualTo(120);
        assertThat(demande.getValue().getCaracteristique()).isEqualTo(2);
        assertThat(demande.getValue().getDateProduction()).isEqualTo(LocalDate.of(2026, 3, 15));
        assertThat(demande.getValue().getExploitation().getIdExploitation()).isEqualTo(idExploitation);
        assertThat(demande.getValue().getZone().getIdZone()).isEqualTo(idZone);
        assertThat(demande.getValue().getIdLot()).isNull();
    }

    @Test
    void createLot_sansStatut_laisseLeDomaineChoisirLeStatutParDefaut() throws Exception {
        when(service.createLot(any(Lot.class))).thenReturn(lot);

        mockMvc.perform(post("/api/lots").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isCreated());

        ArgumentCaptor<Lot> demande = ArgumentCaptor.forClass(Lot.class);
        verify(service).createLot(demande.capture());
        assertThat(demande.getValue().getStatus()).isNull();
        assertThat(demande.getValue().getExploitation()).isNull();
        assertThat(demande.getValue().getZone()).isNull();
    }

    @Test
    void createLot_unStatutInconnuEstRefuse() {
        assertThatThrownBy(() -> mockMvc.perform(post("/api/lots").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"PERIME\"}")))
                .isInstanceOf(ServletException.class)
                .hasRootCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createLot_uneErreurMetierEstPropagee() {
        when(service.createLot(any(Lot.class)))
                .thenThrow(new IllegalArgumentException("L'id de l'exploitation est obligatoire pour enregistrer un lot"));

        assertThatThrownBy(() -> mockMvc.perform(post("/api/lots").contentType(MediaType.APPLICATION_JSON).content("{}")))
                .isInstanceOf(ServletException.class)
                .hasMessageContaining("L'id de l'exploitation est obligatoire pour enregistrer un lot");
    }

    // ------------------------------------------------------------------ consultation
    @Test
    void getLotById_renvoieLeLotAvecSonHistorique() throws Exception {
        when(service.getLotById(idLot)).thenReturn(Optional.of(lot));

        mockMvc.perform(get("/api/lots/getLot/{id}", idLot))
                .andExpect(status().isOk())
                .andExpect(content().json(lotJson(), true));
    }

    @Test
    void getLotById_inconnu_renvoie404SansCorps() throws Exception {
        when(service.getLotById(idLot)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/lots/getLot/{id}", idLot))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    void getLotById_unIdentifiantMalFormeEstUneMauvaiseRequete() throws Exception {
        mockMvc.perform(get("/api/lots/getLot/{id}", "pas-un-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void getAllLots_renvoieLaListeDansLOrdreDuService() throws Exception {
        Lot autre = Lot.builder().idLot(UUID.randomUUID()).status(StatusEnum.ALERT).quantite(5)
                .zone(Zone.builder().idZone(idZone).build()).build();
        when(service.getAllLotsForAllEntrepot()).thenReturn(List.of(lot, autre));

        MvcResult resultat = mockMvc.perform(get("/api/lots/allLotsFromAllEntrepots"))
                .andExpect(status().isOk()).andReturn();

        String json = resultat.getResponse().getContentAsString();
        assertThat(json).startsWith("[{\"idLot\":\"" + idLot + "\"");
        assertThat(json).contains("\"idLot\":\"" + autre.getIdLot() + "\",\"status\":\"ALERT\",\"quantite\":5");
    }

    @Test
    void getAllLots_sansLot_renvoieUneListeVide() throws Exception {
        when(service.getAllLotsForAllEntrepot()).thenReturn(List.of());

        mockMvc.perform(get("/api/lots/allLotsFromAllEntrepots"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]", true));
    }

    @Test
    void getInventaireMondial_renvoieLaQuantiteTotale() throws Exception {
        when(service.getQuantiteTotale()).thenReturn(150);

        mockMvc.perform(get("/api/lots/getAllQuantity"))
                .andExpect(status().isOk())
                .andExpect(content().string("150"));
    }

    @Test
    void getOldestLot_renvoieLeLotLePlusAncien() throws Exception {
        when(service.getOldestLot()).thenReturn(Optional.of(lot));

        mockMvc.perform(get("/api/lots/oldestLot"))
                .andExpect(status().isOk())
                .andExpect(content().json(lotJson(), true));
    }

    @Test
    void getOldestLot_sansLotConforme_renvoie404() throws Exception {
        when(service.getOldestLot()).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/lots/oldestLot")).andExpect(status().isNotFound());
    }

    @Test
    void getAllMesuresByLotId_renvoieLesMesuresDuLotSansIdentifiantNiCapteur() throws Exception {
        Mesure mesure = Mesure.builder().idMesure(UUID.randomUUID()).temperature(new BigDecimal("20.50"))
                .humidite(new BigDecimal("55.00")).dateMesure(LocalDateTime.of(2025, 1, 15, 12, 0)).build();
        when(service.getAllMesuresByLotId(idLot)).thenReturn(List.of(mesure));

        mockMvc.perform(get("/api/lots/getAllMesuresByLotId/{id}", idLot))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"temperature\":20.50,\"humidite\":55.00,\"dateMesure\":\"2025-01-15T12:00:00\"}]", true));
    }

    @Test
    void getAllMesuresByLotId_sansMesure_renvoie404() throws Exception {
        when(service.getAllMesuresByLotId(idLot)).thenReturn(List.of());

        mockMvc.perform(get("/api/lots/getAllMesuresByLotId/{id}", idLot)).andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------ suppression
    @Test
    void deleteLot_renvoie200AvecLIdentifiant() throws Exception {
        when(service.deleteLot(idLot)).thenReturn(Optional.of(lot));

        mockMvc.perform(delete("/api/lots/deleteLot/{id}", idLot))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"idLot\":\"%s\",\"status\":\"OK\"}".formatted(idLot), true));
    }

    @Test
    void deleteLot_inconnu_renvoie404AvecUnCorpsNotFound() throws Exception {
        when(service.deleteLot(idLot)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/lots/deleteLot/{id}", idLot))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"idLot\":null,\"status\":\"NOT_FOUND\"}", true));
    }

    // ------------------------------------------------------------------ transfert
    @Test
    void updateEmplacementLot_renvoie200AvecLIdentifiant() throws Exception {
        UUID destination = UUID.randomUUID();
        when(service.updateEmplacementLot(idLot, destination)).thenReturn(Optional.of(lot));

        mockMvc.perform(put("/api/lots/{id}/zone", idLot).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idZone\":\"%s\"}".formatted(destination)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"idLot\":\"%s\",\"status\":\"OK\"}".formatted(idLot), true));
    }

    @Test
    void updateEmplacementLot_lotInconnu_renvoie404AvecUnCorpsNotFound() throws Exception {
        when(service.updateEmplacementLot(any(), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/lots/{id}/zone", idLot).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idZone\":\"%s\"}".formatted(UUID.randomUUID())))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"idLot\":null,\"status\":\"NOT_FOUND\"}", true));
    }

    @Test
    void updateEmplacementLot_sansZone_transmetUneDestinationAbsente() throws Exception {
        when(service.updateEmplacementLot(idLot, null)).thenReturn(Optional.of(lot));

        mockMvc.perform(put("/api/lots/{id}/zone", idLot).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());

        verify(service).updateEmplacementLot(idLot, null);
    }

    @Test
    void updateEmplacementLot_transfertInvalide_renvoie400AvecLeMessageDeLErreur() throws Exception {
        when(service.updateEmplacementLot(any(), any())).thenThrow(new TransfertLotInvalideException(
                "Erreur de transfert : l'emplacement de destination est identique à l'emplacement actuel."));

        MvcResult resultat = mockMvc.perform(put("/api/lots/{id}/zone", idLot).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idZone\":\"%s\"}".formatted(idZone)))
                .andExpect(status().isBadRequest()).andReturn();

        assertThat(resultat.getResponse().getErrorMessage())
                .isEqualTo("Erreur de transfert : l'emplacement de destination est identique à l'emplacement actuel.");
    }

    // ------------------------------------------------------------------ statut
    @Test
    void updateStatusLot_changeLeStatut() throws Exception {
        when(service.updateStatusLot(idLot, StatusEnum.BLOCKED)).thenReturn(Optional.of(lot));

        mockMvc.perform(put("/api/lots/{id}/status", idLot).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"BLOCKED\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"idLot\":\"%s\",\"status\":\"OK\"}".formatted(idLot), true));
    }

    @Test
    void updateStatusLot_sansStatut_transmetUnStatutAbsent() throws Exception {
        when(service.updateStatusLot(idLot, null)).thenReturn(Optional.of(lot));

        mockMvc.perform(put("/api/lots/{id}/status", idLot).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());

        verify(service).updateStatusLot(idLot, null);
    }

    @Test
    void updateStatusLot_lotInconnu_renvoie404AvecUnCorpsNotFound() throws Exception {
        when(service.updateStatusLot(any(), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/lots/{id}/status", idLot).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"BLOCKED\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"idLot\":null,\"status\":\"NOT_FOUND\"}", true));
    }

    @Test
    void updateStatusLot_unStatutInconnuEstRefuse() {
        assertThatThrownBy(() -> mockMvc.perform(put("/api/lots/{id}/status", idLot).contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"PERIME\"}")))
                .isInstanceOf(ServletException.class)
                .hasRootCauseInstanceOf(IllegalArgumentException.class);
    }

    // ------------------------------------------------------------------ sortie de stock
    @Test
    void stockOut_renvoie201AvecLIdentifiantDeLHistorique() throws Exception {
        when(service.stockOut(idLot)).thenReturn(Historique.builder().idHistorique(idHistorique).type(TypeHistoriqueEnum.SHIPPED).build());

        mockMvc.perform(post("/api/lots/stock-out").param("lotId", idLot.toString()))
                .andExpect(status().isCreated())
                .andExpect(content().json("{\"idHistorique\":\"%s\",\"status\":\"CREATED\"}".formatted(idHistorique), true));
    }

    @Test
    void stockOut_lotInconnu_estUneErreurServeur() {
        when(service.stockOut(idLot)).thenThrow(new LotIntrouvableException(idLot));

        assertThatThrownBy(() -> mockMvc.perform(post("/api/lots/stock-out").param("lotId", idLot.toString())))
                .isInstanceOf(ServletException.class)
                .hasRootCauseInstanceOf(LotIntrouvableException.class);
    }

    @Test
    void stockOut_exigeLIdentifiantDuLot() throws Exception {
        mockMvc.perform(post("/api/lots/stock-out")).andExpect(status().isBadRequest());
    }
}
