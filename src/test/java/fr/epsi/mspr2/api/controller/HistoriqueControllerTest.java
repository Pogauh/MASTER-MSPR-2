package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.api.mapper.HistoriqueMapperImpl;
import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.domain.services.HistoriqueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HistoriqueController.class)
@Import(HistoriqueMapperImpl.class)
class HistoriqueControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockBean
    HistoriqueService service;

    final UUID idLot = UUID.fromString("f1f1f1f1-f1f1-f1f1-f1f1-f1f1f1f1f1f1");
    final UUID idHistorique = UUID.fromString("e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1");
    final UUID zoneA = UUID.fromString("11111111-1111-1111-1111-111111111111");
    final UUID zoneB = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private Historique transfert() {
        return Historique.builder().idHistorique(idHistorique).type(TypeHistoriqueEnum.TRANSFERT)
                .dateEmission(LocalDateTime.of(2025, 1, 20, 10, 0)).zonePrecedente(zoneB).zoneActuelle(zoneA)
                .detail("Transfert du lot").idLot(idLot).build();
    }

    private String transfertJson() {
        return """
                [{"idHistorique":"%s","type":"TRANSFERT","dateEmission":"2025-01-20T10:00:00",
                  "zonePrecedente":"%s","zoneActuelle":"%s","detail":"Transfert du lot"}]
                """.formatted(idHistorique, zoneB, zoneA);
    }

    @Test
    void sansFiltre_renvoieToutLHistoriqueDuLot() throws Exception {
        when(service.findAllHistoriquesForLot(idLot)).thenReturn(List.of(transfert()));

        mockMvc.perform(get("/api/historique/allHistoriquesForLot/{id}", idLot))
                .andExpect(status().isOk())
                .andExpect(content().json(transfertJson(), true));
    }

    @Test
    void unTypeVideEquivautAUneAbsenceDeFiltre() throws Exception {
        when(service.findAllHistoriquesForLot(idLot)).thenReturn(List.of(transfert()));

        mockMvc.perform(get("/api/historique/allHistoriquesForLot/{id}", idLot).param("type", "  "))
                .andExpect(status().isOk())
                .andExpect(content().json(transfertJson(), true));

        verify(service, never()).findAllHistoriquesForLotByType(any(), any());
    }

    @Test
    void filtreParType_sansTenirCompteDeLaCasseNiDesEspaces() throws Exception {
        when(service.findAllHistoriquesForLotByType(idLot, TypeHistoriqueEnum.TRANSFERT)).thenReturn(List.of(transfert()));

        mockMvc.perform(get("/api/historique/allHistoriquesForLot/{id}", idLot).param("type", " transfert "))
                .andExpect(status().isOk())
                .andExpect(content().json(transfertJson(), true));
    }

    @Test
    void alertEstUnAliasDeAlerte() throws Exception {
        when(service.findAllHistoriquesForLotByType(idLot, TypeHistoriqueEnum.ALERTE)).thenReturn(List.of());

        mockMvc.perform(get("/api/historique/allHistoriquesForLot/{id}", idLot).param("type", "alert"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]", true));

        verify(service).findAllHistoriquesForLotByType(idLot, TypeHistoriqueEnum.ALERTE);
    }

    @Test
    void unTypeInconnu_renvoie400SansCorps() throws Exception {
        mockMvc.perform(get("/api/historique/allHistoriquesForLot/{id}", idLot).param("type", "NOPE"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }
}
