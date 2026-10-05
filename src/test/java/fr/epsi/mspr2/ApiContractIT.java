package fr.epsi.mspr2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test de contrat "boîte noire" de l'API REST / MQTT du backend pays.
 *
 * <p>Il ne connaît que les endpoints HTTP, le canal MQTT d'entrée et le schéma SQL : il doit donc rester vert
 * quelle que soit l'organisation interne du code (c'est son rôle de filet de sécurité lors d'une refactorisation).
 * Chaque réponse JSON est comparée exactement (aucun champ en plus, aucun champ en moins).
 */
class ApiContractIT extends IntegrationTestBase {

    // ---------------------------------------------------------------- jeu de données
    static final UUID ENTREPOT_1 = UUID.fromString("770e8400-e29b-41d4-a716-446655440011");
    static final UUID ENTREPOT_2 = UUID.fromString("3f7c9e8a-6b2d-4d91-9a5e-2c8f1b7e4a63");
    static final UUID EXPLOITATION = UUID.fromString("aa0e8400-e29b-41d4-a716-446655440022");
    static final UUID ZONE_A = UUID.fromString("11111111-1111-1111-1111-111111111111");
    static final UUID ZONE_B = UUID.fromString("22222222-2222-2222-2222-222222222222");
    static final UUID ZONE_C = UUID.fromString("33333333-3333-3333-3333-333333333333");
    static final UUID CAPTEUR_A = UUID.fromString("a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1");
    static final UUID CAPTEUR_B = UUID.fromString("b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2");
    static final UUID CAPTEUR_C = UUID.fromString("c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3");
    static final UUID LOT_1 = UUID.fromString("f1f1f1f1-f1f1-f1f1-f1f1-f1f1f1f1f1f1");
    static final UUID LOT_2 = UUID.fromString("f2f2f2f2-f2f2-f2f2-f2f2-f2f2f2f2f2f2");
    static final UUID LOT_3 = UUID.fromString("f3f3f3f3-f3f3-f3f3-f3f3-f3f3f3f3f3f3");
    static final UUID HISTORIQUE_1 = UUID.fromString("e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1");
    static final UUID MESURE_B1 = UUID.fromString("d1d1d1d1-d1d1-d1d1-d1d1-d1d1d1d1d1d1");
    static final UUID MESURE_A1 = UUID.fromString("d2d2d2d2-d2d2-d2d2-d2d2-d2d2d2d2d2d2");
    static final UUID MESURE_A2 = UUID.fromString("d3d3d3d3-d3d3-d3d3-d3d3-d3d3d3d3d3d3");
    static final UUID ALERTE_1 = UUID.fromString("c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1");
    static final UUID INCONNU = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    @Qualifier("mqttInputChannel")
    MessageChannel mqttInputChannel;

    @BeforeEach
    void seed() {
        viderLaBase();
        jdbc.update("insert into entrepot (id_entrepot, localisation, nom) values (?, ?, ?)", ENTREPOT_1, "Bresil", "Entrepot Principal");
        jdbc.update("insert into entrepot (id_entrepot, localisation, nom) values (?, ?, ?)", ENTREPOT_2, "Bresil", "Entrepot Secondaire");
        jdbc.update("insert into exploitation (id_exploitation, nom, adresse) values (?, ?, ?)", EXPLOITATION, "Exploitation Flamenco", "804 rue du Barista");
        zone(ZONE_A, "A", ENTREPOT_1);
        zone(ZONE_B, "B", ENTREPOT_1);
        zone(ZONE_C, "C", ENTREPOT_2);
        capteur(CAPTEUR_A, "02:AA:BB:CC:DD:01", ZONE_A);
        capteur(CAPTEUR_B, "02:AA:BB:CC:DD:02", ZONE_B);
        capteur(CAPTEUR_C, "02:AA:BB:CC:DD:03", ZONE_C);
        lot(LOT_1, "COMPLIANT", 100, 2, "2025-01-10T08:00:00", "2025-01-01", ZONE_A);
        lot(LOT_2, "COMPLIANT", 200, 3, "2025-03-01T10:30:00", "2025-02-20", ZONE_B);
        lot(LOT_3, "ALERT", 50, 1, "2025-02-01T09:00:00", "2025-01-15", ZONE_A);
        jdbc.update("insert into historique (id_historique, type, date_emission, zone_precedente, zone_actuelle, detail, id_lot) values (?, ?, ?, ?, ?, ?, ?)",
                HISTORIQUE_1, "TRANSFERT", ts("2025-01-20T10:00:00"), ZONE_B, ZONE_A, "Transfert du lot", LOT_1);
        mesure(MESURE_B1, "20.50", "55.00", "2025-01-15T12:00:00", CAPTEUR_B);
        mesure(MESURE_A1, "28.00", "54.00", "2025-02-01T00:00:00", CAPTEUR_A);
        mesure(MESURE_A2, "29.50", "56.25", "2025-02-02T00:00:00", CAPTEUR_A);
        jdbc.update("insert into alerte (id_alerte, type_alerte, description, date_emission, id_capteur) values (?, ?, ?, ?, ?)",
                ALERTE_1, "HAUTETEMPERATURE", "Temperature trop haute", ts("2025-02-03T10:00:00"), CAPTEUR_A);
    }

    // ---------------------------------------------------------------- lots
    @Nested
    @DisplayName("/api/lots")
    class Lots {

        @Test
        void getLot_renvoieLeLotAvecSonHistorique() throws Exception {
            assertJson(get("/api/lots/getLot/{id}", LOT_1), 200, """
                    {"idLot":"%s","status":"COMPLIANT","quantite":100,"caracteristique":2,"dateSortie":null,
                     "dateStockage":"2025-01-10T08:00:00","dateProduction":"2025-01-01","emplacement":"%s",
                     "idExploitation":"%s","idEntrepot":"%s",
                     "historique":[{"idHistorique":"%s","type":"TRANSFERT","dateEmission":"2025-01-20T10:00:00",
                                    "zonePrecedente":"%s","zoneActuelle":"%s","detail":"Transfert du lot"}]}
                    """.formatted(LOT_1, ZONE_A, EXPLOITATION, ENTREPOT_1, HISTORIQUE_1, ZONE_B, ZONE_A));
        }

        @Test
        void getLot_inconnu_renvoie404SansCorps() throws Exception {
            mockMvc.perform(get("/api/lots/getLot/{id}", INCONNU))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(""));
        }

        @Test
        void allLotsFromAllEntrepots_renvoieTousLesLotsTriesParDateDeStockage() throws Exception {
            String json = mockMvc.perform(get("/api/lots/allLotsFromAllEntrepots"))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

            JSONAssert.assertEquals("""
                    [{"idLot":"%s","status":"COMPLIANT","quantite":100,"caracteristique":2,"dateSortie":null,
                      "dateStockage":"2025-01-10T08:00:00","dateProduction":"2025-01-01","emplacement":"%s",
                      "idExploitation":"%s","idEntrepot":"%s",
                      "historique":[{"idHistorique":"%s","type":"TRANSFERT","dateEmission":"2025-01-20T10:00:00",
                                     "zonePrecedente":"%s","zoneActuelle":"%s","detail":"Transfert du lot"}]},
                     {"idLot":"%s","status":"ALERT","quantite":50,"caracteristique":1,"dateSortie":null,
                      "dateStockage":"2025-02-01T09:00:00","dateProduction":"2025-01-15","emplacement":"%s",
                      "idExploitation":"%s","idEntrepot":"%s","historique":[]},
                     {"idLot":"%s","status":"COMPLIANT","quantite":200,"caracteristique":3,"dateSortie":null,
                      "dateStockage":"2025-03-01T10:30:00","dateProduction":"2025-02-20","emplacement":"%s",
                      "idExploitation":"%s","idEntrepot":"%s","historique":[]}]
                    """.formatted(LOT_1, ZONE_A, EXPLOITATION, ENTREPOT_1, HISTORIQUE_1, ZONE_B, ZONE_A,
                    LOT_3, ZONE_A, EXPLOITATION, ENTREPOT_1,
                    LOT_2, ZONE_B, EXPLOITATION, ENTREPOT_1), json, JSONCompareMode.STRICT);
        }

        @Test
        void getAllQuantity_additionneLesQuantites() throws Exception {
            mockMvc.perform(get("/api/lots/getAllQuantity"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("350"));
        }

        @Test
        void oldestLot_renvoieLePlusAncienLotConforme() throws Exception {
            String json = mockMvc.perform(get("/api/lots/oldestLot"))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertThat(objectMapper.readTree(json).get("idLot").asText()).isEqualTo(LOT_1.toString());
            assertThat(objectMapper.readTree(json).get("historique")).hasSize(1);
        }

        @Test
        void oldestLot_sansLotConforme_renvoie404() throws Exception {
            jdbc.update("update lot set status = 'SHIPPED'");
            mockMvc.perform(get("/api/lots/oldestLot")).andExpect(status().isNotFound());
        }

        @Test
        void getAllMesuresByLotId_reconstitueLeParcoursDuLotDeZoneEnZone() throws Exception {
            assertJson(get("/api/lots/getAllMesuresByLotId/{id}", LOT_1), 200, """
                    [{"temperature":20.50,"humidite":55.00,"dateMesure":"2025-01-15T12:00:00"},
                     {"temperature":28.00,"humidite":54.00,"dateMesure":"2025-02-01T00:00:00"},
                     {"temperature":29.50,"humidite":56.25,"dateMesure":"2025-02-02T00:00:00"}]
                    """);
        }

        @Test
        void getAllMesuresByLotId_sansMesure_renvoie404() throws Exception {
            jdbc.update("delete from mesure");
            mockMvc.perform(get("/api/lots/getAllMesuresByLotId/{id}", LOT_1)).andExpect(status().isNotFound());
        }

        @Test
        void createLot_renvoie201EtPersisteLeLot() throws Exception {
            String body = """
                    {"status":"COMPLIANT","quantite":40,"caracteristique":1,"dateProduction":"2026-01-02",
                     "idExploitation":"%s","idZone":"%s"}
                    """.formatted(EXPLOITATION, ZONE_C);

            MvcResult result = mockMvc.perform(post("/api/lots").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isCreated()).andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
            assertThat(response.fieldNames()).toIterable().containsExactlyInAnyOrder("idLot", "status");
            assertThat(response.get("status").asText()).isEqualTo("CREATED");
            UUID id = UUID.fromString(response.get("idLot").asText());

            Map<String, Object> row = jdbc.queryForMap("select * from lot where id_lot = ?", id);
            assertThat(row.get("status")).isEqualTo("COMPLIANT");
            assertThat(row.get("quantite")).isEqualTo(40);
            assertThat(row.get("caracteristique")).isEqualTo(1);
            assertThat(row.get("date_production").toString()).isEqualTo("2026-01-02");
            assertThat(row.get("id_exploitation")).isEqualTo(EXPLOITATION);
            assertThat(row.get("id_zone")).isEqualTo(ZONE_C);
            assertThat(row.get("date_sortie")).isNull();
            // la date de stockage est toujours posée par le serveur
            assertThat(((Timestamp) row.get("date_stockage")).toLocalDateTime()).isAfter(LocalDateTime.now().minusMinutes(1));
        }

        @Test
        void createLot_sansStatut_prendLeStatutConformeParDefaut() throws Exception {
            String body = """
                    {"quantite":10,"idExploitation":"%s","idZone":"%s"}
                    """.formatted(EXPLOITATION, ZONE_A);

            MvcResult result = mockMvc.perform(post("/api/lots").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isCreated()).andReturn();

            UUID id = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("idLot").asText());
            assertThat(jdbc.queryForObject("select status from lot where id_lot = ?", String.class, id)).isEqualTo("COMPLIANT");
        }

        @Test
        void createLot_sansExploitation_estRejeteEtNonPersiste() {
            String body = """
                    {"status":"COMPLIANT","quantite":40,"idZone":"%s"}
                    """.formatted(ZONE_C);

            assertThatThrownBy(() -> mockMvc.perform(post("/api/lots").contentType(MediaType.APPLICATION_JSON).content(body)))
                    .isInstanceOf(ServletException.class)
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("L'id de l'exploitation est obligatoire pour enregistrer un lot");
            assertThat(jdbc.queryForObject("select count(*) from lot", Integer.class)).isEqualTo(3);
        }

        @Test
        void updateZone_deplaceLeLotEtTraceLeTransfert() throws Exception {
            assertJson(put("/api/lots/{id}/zone", LOT_2).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"idZone\":\"%s\"}".formatted(ZONE_C)), 200,
                    "{\"idLot\":\"%s\",\"status\":\"OK\"}".formatted(LOT_2));

            assertThat(jdbc.queryForObject("select id_zone from lot where id_lot = ?", UUID.class, LOT_2)).isEqualTo(ZONE_C);
            Map<String, Object> historique = jdbc.queryForMap("select * from historique where id_lot = ?", LOT_2);
            assertThat(historique.get("type")).isEqualTo("TRANSFERT");
            assertThat(historique.get("zone_precedente")).isEqualTo(ZONE_B);
            assertThat(historique.get("zone_actuelle")).isEqualTo(ZONE_C);
            assertThat(historique.get("detail")).isEqualTo("Transfert du lot " + LOT_2 + " de la zone " + ZONE_B + " vers la zone " + ZONE_C);
            assertThat(((Timestamp) historique.get("date_emission")).toLocalDateTime()).isAfter(LocalDateTime.now().minusMinutes(1));
        }

        @Test
        void updateZone_versLaMemeZone_renvoie400() throws Exception {
            MvcResult result = mockMvc.perform(put("/api/lots/{id}/zone", LOT_2).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"idZone\":\"%s\"}".formatted(ZONE_B)))
                    .andExpect(status().isBadRequest()).andReturn();
            assertThat(result.getResponse().getErrorMessage())
                    .isEqualTo("Erreur de transfert : l'emplacement de destination est identique à l'emplacement actuel.");
            assertThat(jdbc.queryForObject("select count(*) from historique where id_lot = ?", Integer.class, LOT_2)).isZero();
        }

        @Test
        void updateZone_lotInconnu_renvoie404AvecCorpsNotFound() throws Exception {
            assertJson(put("/api/lots/{id}/zone", INCONNU).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"idZone\":\"%s\"}".formatted(ZONE_C)), 404,
                    "{\"idLot\":null,\"status\":\"NOT_FOUND\"}");
        }

        @Test
        void updateStatus_changeLeStatut() throws Exception {
            assertJson(put("/api/lots/{id}/status", LOT_2).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"status\":\"BLOCKED\"}"), 200,
                    "{\"idLot\":\"%s\",\"status\":\"OK\"}".formatted(LOT_2));
            assertThat(jdbc.queryForObject("select status from lot where id_lot = ?", String.class, LOT_2)).isEqualTo("BLOCKED");
        }

        @Test
        void updateStatus_sansStatut_neChangeRien() throws Exception {
            assertJson(put("/api/lots/{id}/status", LOT_2).contentType(MediaType.APPLICATION_JSON).content("{}"), 200,
                    "{\"idLot\":\"%s\",\"status\":\"OK\"}".formatted(LOT_2));
            assertThat(jdbc.queryForObject("select status from lot where id_lot = ?", String.class, LOT_2)).isEqualTo("COMPLIANT");
        }

        @Test
        void updateStatus_lotInconnu_renvoie404AvecCorpsNotFound() throws Exception {
            assertJson(put("/api/lots/{id}/status", INCONNU).contentType(MediaType.APPLICATION_JSON)
                    .content("{\"status\":\"BLOCKED\"}"), 404, "{\"idLot\":null,\"status\":\"NOT_FOUND\"}");
        }

        @Test
        void deleteLot_supprimeLeLot() throws Exception {
            assertJson(delete("/api/lots/deleteLot/{id}", LOT_2), 200,
                    "{\"idLot\":\"%s\",\"status\":\"OK\"}".formatted(LOT_2));
            assertThat(jdbc.queryForObject("select count(*) from lot where id_lot = ?", Integer.class, LOT_2)).isZero();
        }

        @Test
        void deleteLot_inconnu_renvoie404AvecCorpsNotFound() throws Exception {
            assertJson(delete("/api/lots/deleteLot/{id}", INCONNU), 404, "{\"idLot\":null,\"status\":\"NOT_FOUND\"}");
        }

        @Test
        void stockOut_expedieLeLotEtCreeUnHistorique() throws Exception {
            MvcResult result = mockMvc.perform(post("/api/lots/stock-out").param("lotId", LOT_2.toString()))
                    .andExpect(status().isCreated()).andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
            assertThat(response.fieldNames()).toIterable().containsExactlyInAnyOrder("idHistorique", "status");
            assertThat(response.get("status").asText()).isEqualTo("CREATED");
            UUID idHistorique = UUID.fromString(response.get("idHistorique").asText());

            Map<String, Object> lot = jdbc.queryForMap("select * from lot where id_lot = ?", LOT_2);
            assertThat(lot.get("status")).isEqualTo("SHIPPED");
            assertThat(((Timestamp) lot.get("date_sortie")).toLocalDateTime()).isAfter(LocalDateTime.now().minusMinutes(1));

            Map<String, Object> historique = jdbc.queryForMap("select * from historique where id_historique = ?", idHistorique);
            assertThat(historique.get("type")).isEqualTo("SHIPPED");
            assertThat(historique.get("detail")).isEqualTo("Le lot a été expédié.");
            assertThat(historique.get("id_lot")).isEqualTo(LOT_2);
            assertThat(historique.get("zone_precedente")).isNull();
            assertThat(historique.get("zone_actuelle")).isNull();
        }

        @Test
        void stockOut_lotInconnu_finitEnErreurServeur() {
            assertThatThrownBy(() -> mockMvc.perform(post("/api/lots/stock-out").param("lotId", INCONNU.toString())))
                    .isInstanceOf(ServletException.class);
            assertThat(jdbc.queryForObject("select count(*) from historique", Integer.class)).isEqualTo(1);
        }
    }

    // ---------------------------------------------------------------- historique
    @Nested
    @DisplayName("/api/historique")
    class Historiques {

        private static final String LOT_1_HISTORIQUE = """
                [{"idHistorique":"%s","type":"TRANSFERT","dateEmission":"2025-01-20T10:00:00",
                  "zonePrecedente":"%s","zoneActuelle":"%s","detail":"Transfert du lot"}]
                """;

        @Test
        void sansFiltre_renvoieTousLesHistoriquesDuLot() throws Exception {
            assertJson(get("/api/historique/allHistoriquesForLot/{id}", LOT_1), 200,
                    LOT_1_HISTORIQUE.formatted(HISTORIQUE_1, ZONE_B, ZONE_A));
        }

        @Test
        void filtreParType_estInsensibleALaCasse() throws Exception {
            assertJson(get("/api/historique/allHistoriquesForLot/{id}", LOT_1).param("type", " transfert "), 200,
                    LOT_1_HISTORIQUE.formatted(HISTORIQUE_1, ZONE_B, ZONE_A));
            assertJson(get("/api/historique/allHistoriquesForLot/{id}", LOT_1).param("type", "SHIPPED"), 200, "[]");
        }

        @Test
        void alert_estUnAliasDeAlerte() throws Exception {
            jdbc.update("insert into historique (id_historique, type, date_emission, zone_precedente, zone_actuelle, detail, id_lot) values (?, ?, ?, ?, ?, ?, ?)",
                    UUID.randomUUID(), "ALERTE", ts("2025-02-03T10:00:00"), ZONE_A, ZONE_A, ALERTE_1.toString(), LOT_1);

            String json = mockMvc.perform(get("/api/historique/allHistoriquesForLot/{id}", LOT_1).param("type", "ALERT"))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertThat(objectMapper.readTree(json)).hasSize(1);
            assertThat(objectMapper.readTree(json).get(0).get("type").asText()).isEqualTo("ALERTE");
        }

        @Test
        void typeInconnu_renvoie400SansCorps() throws Exception {
            mockMvc.perform(get("/api/historique/allHistoriquesForLot/{id}", LOT_1).param("type", "NOPE"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(""));
        }
    }

    // ---------------------------------------------------------------- référentiel
    @Nested
    @DisplayName("Référentiel : entrepôts, zones, exploitations")
    class Referentiel {

        @Test
        void entrepots() throws Exception {
            assertJson(get("/api/entrepots"), 200, """
                    [{"idEntrepot":"%s","localisation":"Bresil","nom":"Entrepot Principal"},
                     {"idEntrepot":"%s","localisation":"Bresil","nom":"Entrepot Secondaire"}]
                    """.formatted(ENTREPOT_1, ENTREPOT_2));
        }

        @Test
        void exploitations() throws Exception {
            assertJson(get("/api/exploitation/allExploitations"), 200, """
                    [{"idExploitation":"%s","nom":"Exploitation Flamenco","adresse":"804 rue du Barista"}]
                    """.formatted(EXPLOITATION));
        }

        @Test
        void zonesDUnEntrepot_avecCapteurEtNombreDeLots() throws Exception {
            assertJson(get("/api/entrepots/{id}/zones", ENTREPOT_1), 200, """
                    [{"idZone":"%s","nomZone":"A","idEntrepot":"%s","idCapteur":"%s","lotCount":2},
                     {"idZone":"%s","nomZone":"B","idEntrepot":"%s","idCapteur":"%s","lotCount":1}]
                    """.formatted(ZONE_A, ENTREPOT_1, CAPTEUR_A, ZONE_B, ENTREPOT_1, CAPTEUR_B));
        }

        @Test
        void zonesDUnEntrepotInconnu_renvoieListeVide() throws Exception {
            assertJson(get("/api/entrepots/{id}/zones", INCONNU), 200, "[]");
        }

        @Test
        void resumeDesCapteursDUnEntrepot_moyennesSur24h() throws Exception {
            LocalDateTime now = LocalDateTime.now();
            jdbc.update("delete from mesure");
            mesure(UUID.randomUUID(), "30.00", "50.00", now.minusHours(30).toString(), CAPTEUR_A);   // hors fenêtre 24 h
            mesure(UUID.randomUUID(), "20.00", "40.00", now.minusHours(5).toString(), CAPTEUR_A);
            mesure(UUID.randomUUID(), "25.00", "45.00", now.minusHours(1).toString(), CAPTEUR_A);    // dernière mesure

            assertJson(get("/api/entrepots/{id}/capteurSummary", ENTREPOT_1), 200, """
                    [{"zoneId":"%s","zoneName":"A","lastTemp":25.00,"lastHumidity":45.00,"avgTemp24h":22.50,"avgHumidity24h":42.50},
                     {"zoneId":"%s","zoneName":"B","lastTemp":null,"lastHumidity":null,"avgTemp24h":null,"avgHumidity24h":null}]
                    """.formatted(ZONE_A, ZONE_B));
        }
    }

    // ---------------------------------------------------------------- mesures / capteurs / zones
    @Nested
    @DisplayName("Mesures, capteurs, zones")
    class Mesures {

        @Test
        void getMesure() throws Exception {
            assertJson(get("/api/mesures/getMesure/{id}", MESURE_A2), 200, """
                    {"idMesure":"%s","temperature":29.50,"humidite":56.25,"dateMesure":"2025-02-02T00:00:00","idCapteur":"%s"}
                    """.formatted(MESURE_A2, CAPTEUR_A));
        }

        @Test
        void getMesureInconnue_renvoie200AvecCorpsVide() throws Exception {
            mockMvc.perform(get("/api/mesures/getMesure/{id}", INCONNU))
                    .andExpect(status().isOk())
                    .andExpect(content().string(""));
        }

        @Test
        void deleteMesure() throws Exception {
            assertJson(delete("/api/mesures/deleteMesure/{id}", MESURE_A1), 200,
                    "{\"idMesure\":\"%s\",\"status\":\"OK\"}".formatted(MESURE_A1));
            assertThat(jdbc.queryForObject("select count(*) from mesure where id_mesure = ?", Integer.class, MESURE_A1)).isZero();
        }

        @Test
        void deleteMesureInconnue_renvoie404AvecCorpsNotFound() throws Exception {
            assertJson(delete("/api/mesures/deleteMesure/{id}", INCONNU), 404, "{\"idMesure\":null,\"status\":\"NOT_FOUND\"}");
        }

        @Test
        void mesuresDUnCapteur() throws Exception {
            assertJson(get("/api/capteurs/{id}/mesures", CAPTEUR_A), 200, """
                    [{"idMesure":"%s","temperature":28.00,"humidite":54.00,"dateMesure":"2025-02-01T00:00:00","idCapteur":"%s"},
                     {"idMesure":"%s","temperature":29.50,"humidite":56.25,"dateMesure":"2025-02-02T00:00:00","idCapteur":"%s"}]
                    """.formatted(MESURE_A1, CAPTEUR_A, MESURE_A2, CAPTEUR_A));
        }

        @Test
        void mesuresDUneZoneSurUnePeriode() throws Exception {
            assertJson(get("/api/zone/readings/{id}", ZONE_A)
                    .param("startDate", "2025-02-01T12:00:00").param("endDate", "2025-02-03T00:00:00"), 200, """
                    [{"idMesure":"%s","temperature":29.50,"humidite":56.25,"dateMesure":"2025-02-02T00:00:00","idCapteur":"%s"}]
                    """.formatted(MESURE_A2, CAPTEUR_A));
        }

        @Test
        void mesuresDUneZone_periodeInversee_renvoie400() throws Exception {
            mockMvc.perform(get("/api/zone/readings/{id}", ZONE_A)
                            .param("startDate", "2025-02-03T00:00:00").param("endDate", "2025-02-01T00:00:00"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(""));
        }

        @Test
        void mesuresDUneZone_parametresObligatoires() throws Exception {
            mockMvc.perform(get("/api/zone/readings/{id}", ZONE_A)).andExpect(status().isBadRequest());
        }

        @Test
        void derniereMesureDUneZone() throws Exception {
            assertJson(get("/api/zone/{id}/readings/latest", ZONE_A), 200, """
                    {"idMesure":"%s","temperature":29.50,"humidite":56.25,"dateMesure":"2025-02-02T00:00:00","idCapteur":"%s"}
                    """.formatted(MESURE_A2, CAPTEUR_A));
        }

        @Test
        void derniereMesureDUneZoneSansMesure_renvoie404() throws Exception {
            mockMvc.perform(get("/api/zone/{id}/readings/latest", ZONE_C)).andExpect(status().isNotFound());
        }

        @Test
        void createMesure_persisteLaMesure_etRenvoieLeStatutCreated() throws Exception {
            String body = """
                    {"temperature":29.38,"humidite":54.18,"dateMesure":"2026-03-15T15:42:44","idCapteur":"%s"}
                    """.formatted(CAPTEUR_C);

            String json = mockMvc.perform(post("/api/mesures").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

            // Comportement historique : l'identifiant de la mesure créée n'est pas renvoyé (idMesure = null).
            JSONAssert.assertEquals("{\"idMesure\":null,\"status\":\"CREATED\"}", json, JSONCompareMode.STRICT);
            Map<String, Object> row = jdbc.queryForMap("select * from mesure where id_capteur = ?", CAPTEUR_C);
            assertThat(row.get("temperature").toString()).isEqualTo("29.38");
            assertThat(row.get("humidite").toString()).isEqualTo("54.18");
            assertThat(((Timestamp) row.get("date_mesure")).toLocalDateTime()).isEqualTo(LocalDateTime.parse("2026-03-15T15:42:44"));
            verify(mailSender, never()).send(any(SimpleMailMessage.class));
        }

        @Test
        void createMesure_sansDate_prendLaDateDuJour() throws Exception {
            mockMvc.perform(post("/api/mesures").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"temperature\":29,\"humidite\":55,\"idCapteur\":\"%s\"}".formatted(CAPTEUR_C)))
                    .andExpect(status().isCreated());
            LocalDateTime date = jdbc.queryForObject("select date_mesure from mesure where id_capteur = ?", Timestamp.class, CAPTEUR_C).toLocalDateTime();
            assertThat(date).isAfter(LocalDateTime.now().minusMinutes(1));
        }

        @Test
        void createMesure_sansCapteur_estRejetee() {
            assertThatThrownBy(() -> mockMvc.perform(post("/api/mesures").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"temperature\":29,\"humidite\":55}")))
                    .isInstanceOf(ServletException.class)
                    .hasRootCauseInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("idCapteur est obligatoire");
        }

        @Test
        void createMesure_sansTemperature_estRejeteeEtNonPersistee() {
            assertThatThrownBy(() -> mockMvc.perform(post("/api/mesures").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"humidite\":55,\"idCapteur\":\"%s\"}".formatted(CAPTEUR_C))))
                    .isInstanceOf(ServletException.class)
                    .hasMessageContaining("La mesure doit contenir temperature et humidite");
            assertThat(jdbc.queryForObject("select count(*) from mesure where id_capteur = ?", Integer.class, CAPTEUR_C)).isZero();
        }
    }

    // ---------------------------------------------------------------- alertes
    @Nested
    @DisplayName("Alertes")
    class Alertes {

        @Test
        void listeDesAlertesDUnEntrepot() throws Exception {
            assertJson(get("/api/alertes").param("idEntrepot", ENTREPOT_1.toString()), 200, """
                    [{"idAlerte":"%s","typeAlerte":"HAUTETEMPERATURE","description":"Temperature trop haute",
                      "dateEmission":"2025-02-03T10:00:00","idZone":"%s"}]
                    """.formatted(ALERTE_1, ZONE_A));
            assertJson(get("/api/alertes").param("idEntrepot", ENTREPOT_2.toString()), 200, "[]");
        }

        @Test
        void listeDesAlertes_filtreParPeriode() throws Exception {
            assertJson(get("/api/alertes").param("idEntrepot", ENTREPOT_1.toString())
                    .param("startDate", "2025-02-04T00:00:00"), 200, "[]");
            assertJson(get("/api/alertes").param("idEntrepot", ENTREPOT_1.toString())
                    .param("startDate", "2025-02-01T00:00:00").param("endDate", "2025-02-05T00:00:00"), 200, """
                    [{"idAlerte":"%s","typeAlerte":"HAUTETEMPERATURE","description":"Temperature trop haute",
                      "dateEmission":"2025-02-03T10:00:00","idZone":"%s"}]
                    """.formatted(ALERTE_1, ZONE_A));
        }

        @Test
        void listeDesAlertes_idEntrepotObligatoire() throws Exception {
            mockMvc.perform(get("/api/alertes")).andExpect(status().isBadRequest());
        }

        @Test
        void cinqMesuresConsecutivesHorsPlage_declenchentUneAlerte() throws Exception {
            jdbc.update("delete from mesure");
            jdbc.update("delete from alerte");
            for (int i = 0; i < 4; i++) {
                envoyerMesure("35.00", "55.00", "2026-03-15T10:0" + i + ":00");
            }
            assertThat(jdbc.queryForObject("select count(*) from alerte", Integer.class)).isZero();

            envoyerMesure("35.00", "55.00", "2026-03-15T10:04:00");

            Map<String, Object> alerte = jdbc.queryForMap("select * from alerte");
            assertThat(alerte.get("type_alerte")).isEqualTo("HAUTETEMPERATURE");
            assertThat(alerte.get("description")).isEqualTo("Temperature trop haute: 35.00 (ideal=29, tolerance=3)");
            assertThat(alerte.get("id_capteur")).isEqualTo(CAPTEUR_A);

            // les lots de la zone du capteur passent en ALERT, avec une trace dans l'historique
            assertThat(jdbc.queryForList("select id_lot, status from lot where id_zone = ?", ZONE_A))
                    .allSatisfy(lot -> assertThat(lot.get("status")).isEqualTo("ALERT"));
            assertThat(jdbc.queryForObject("select status from lot where id_lot = ?", String.class, LOT_2)).isEqualTo("COMPLIANT");
            List<Map<String, Object>> historiques = jdbc.queryForList(
                    "select * from historique where type = 'ALERTE' order by id_lot");
            assertThat(historiques).hasSize(2).allSatisfy(h -> {
                assertThat(h.get("zone_precedente")).isEqualTo(ZONE_A);
                assertThat(h.get("zone_actuelle")).isEqualTo(ZONE_A);
                assertThat(h.get("detail")).isEqualTo(alerte.get("id_alerte").toString());
            });
            assertThat(historiques).extracting(h -> h.get("id_lot")).containsExactlyInAnyOrder(LOT_1, LOT_3);

            // et le responsable est prévenu par e-mail
            ArgumentCaptor<SimpleMailMessage> mail = ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(mailSender).send(mail.capture());
            assertThat(mail.getValue().getTo()).containsExactly("responsable@futurekawa.test");
            assertThat(mail.getValue().getFrom()).isEqualTo("ultrabaga@hotmail.com");
            assertThat(mail.getValue().getSubject()).isEqualTo("ALERTE SYSTÈME : HAUTETEMPERATURE");
            assertThat(mail.getValue().getText())
                    .contains("- Identifiant de l'alerte : " + alerte.get("id_alerte"))
                    .contains("- Type : HAUTETEMPERATURE")
                    .contains("- Description : Temperature trop haute: 35.00 (ideal=29, tolerance=3)");

            // l'alerte est visible via l'API
            String json = mockMvc.perform(get("/api/alertes").param("idEntrepot", ENTREPOT_1.toString()))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertThat(objectMapper.readTree(json)).hasSize(1);
            assertThat(objectMapper.readTree(json).get(0).get("typeAlerte").asText()).isEqualTo("HAUTETEMPERATURE");
        }

        @Test
        void uneMesureDansLaPlageAuMilieuDesAutres_neDeclenchePasDAlerte() throws Exception {
            jdbc.update("delete from mesure");
            jdbc.update("delete from alerte");
            envoyerMesure("35.00", "55.00", "2026-03-15T10:00:00");
            envoyerMesure("35.00", "55.00", "2026-03-15T10:01:00");
            envoyerMesure("29.00", "55.00", "2026-03-15T10:02:00");
            envoyerMesure("35.00", "55.00", "2026-03-15T10:03:00");
            envoyerMesure("35.00", "55.00", "2026-03-15T10:04:00");

            assertThat(jdbc.queryForObject("select count(*) from alerte", Integer.class)).isZero();
            verify(mailSender, never()).send(any(SimpleMailMessage.class));
        }

        @Test
        void typesDAlerte_selonLaGrandeurHorsPlage() throws Exception {
            assertThat(alerteApresCinqMesures("20.00", "55.00")).containsExactly("BASSETEMPERATURE", "Temperature trop basse: 20.00 (ideal=29, tolerance=3)");
            assertThat(alerteApresCinqMesures("29.00", "70.00")).containsExactly("HAUTEHUMIDITE", "Humidite trop haute: 70.00 (ideal=55, tolerance=2)");
            assertThat(alerteApresCinqMesures("29.00", "40.00")).containsExactly("BASSEHUMIDITE", "Humidite trop basse: 40.00 (ideal=55, tolerance=2)");
        }

        private List<String> alerteApresCinqMesures(String temperature, String humidite) throws Exception {
            jdbc.update("delete from mesure");
            jdbc.update("delete from alerte");
            for (int i = 0; i < 5; i++) {
                envoyerMesure(temperature, humidite, "2026-03-15T10:0" + i + ":00");
            }
            Map<String, Object> alerte = jdbc.queryForMap("select * from alerte");
            return List.of((String) alerte.get("type_alerte"), (String) alerte.get("description"));
        }

        private void envoyerMesure(String temperature, String humidite, String date) throws Exception {
            mockMvc.perform(post("/api/mesures").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"temperature\":%s,\"humidite\":%s,\"dateMesure\":\"%s\",\"idCapteur\":\"%s\"}"
                                    .formatted(temperature, humidite, date, CAPTEUR_A)))
                    .andExpect(status().isCreated());
        }
    }

    // ---------------------------------------------------------------- reporting
    @Nested
    @DisplayName("/api/reporting")
    class Reporting {

        @Test
        void global() throws Exception {
            jdbc.update("update lot set date_stockage = ?", Timestamp.valueOf(LocalDateTime.now().minusDays(10).minusHours(1)));
            jdbc.update("update historique set date_emission = ?", Timestamp.valueOf(LocalDateTime.now().minusDays(2)));

            assertJson(get("/api/reporting/global"), 200, """
                    {"totalLots":3,"avgAgeDays":10.0,"complianceRate":66.66666666666666,"countMouvementLast30Days":1}
                    """);
        }

        @Test
        void quality() throws Exception {
            assertJson(get("/api/reporting/quality")
                    .param("startDate", "2025-02-01T00:00:00").param("endDate", "2025-02-05T00:00:00"), 200, """
                    {"alertCount":1,"complianceRate":66.66666666666666,"nonComplianceCount":1}
                    """);
            assertJson(get("/api/reporting/quality")
                    .param("startDate", "2025-03-01T00:00:00").param("endDate", "2025-03-05T00:00:00"), 200, """
                    {"alertCount":0,"complianceRate":66.66666666666666,"nonComplianceCount":1}
                    """);
        }

        @Test
        void qualityEtStock_exigentLesDeuxDates() throws Exception {
            mockMvc.perform(get("/api/reporting/quality")).andExpect(status().isBadRequest());
            mockMvc.perform(get("/api/reporting/stock").param("startDate", "2025-02-01T00:00:00")).andExpect(status().isBadRequest());
        }

        @Test
        void stock_listeLesLotsAEcouler() throws Exception {
            LocalDateTime now = LocalDateTime.now();
            jdbc.update("update lot set date_stockage = ? where id_lot = ?", Timestamp.valueOf(now.minusMonths(10)), LOT_1);
            jdbc.update("update lot set date_stockage = ? where id_lot = ?", Timestamp.valueOf(now.minusMonths(2)), LOT_2);
            jdbc.update("update lot set date_stockage = ? where id_lot = ?", Timestamp.valueOf(now.minusMonths(11)), LOT_3);

            String json = mockMvc.perform(get("/api/reporting/stock")
                            .param("startDate", "2025-02-01T00:00:00").param("endDate", "2025-02-05T00:00:00"))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

            JsonNode body = objectMapper.readTree(json);
            assertThat(body.fieldNames()).toIterable().containsExactlyInAnyOrder("totalLots", "avgAgeDays", "complianceRate", "lotIdsUrgentFifo");
            assertThat(body.get("totalLots").asInt()).isEqualTo(3);
            assertThat(body.get("complianceRate").asDouble()).isEqualTo(2 / 3.0 * 100);
            assertThat(body.get("lotIdsUrgentFifo")).extracting(JsonNode::asText)
                    .containsExactlyInAnyOrder(LOT_1.toString(), LOT_3.toString());
        }
    }

    // ---------------------------------------------------------------- MQTT
    @Nested
    @DisplayName("Ingestion MQTT")
    class Mqtt {

        @Test
        void unMessageMqttValide_creeUneMesure() {
            String payload = """
                    {"temperature":27.50,"humidite":56.20,"dateMesure":"2026-03-15T16:00:00","idCapteur":"%s"}
                    """.formatted(CAPTEUR_C);

            mqttInputChannel.send(MessageBuilder.withPayload(payload.getBytes(StandardCharsets.UTF_8)).build());

            Map<String, Object> row = jdbc.queryForMap("select * from mesure where id_capteur = ?", CAPTEUR_C);
            assertThat(row.get("temperature").toString()).isEqualTo("27.50");
            assertThat(row.get("humidite").toString()).isEqualTo("56.20");
            assertThat(((Timestamp) row.get("date_mesure")).toLocalDateTime()).isEqualTo(LocalDateTime.parse("2026-03-15T16:00:00"));
        }

        @Test
        void unMessageMqttInvalide_estIgnoreSansPropagerDErreur() {
            mqttInputChannel.send(MessageBuilder.withPayload("pas du json").build());
            mqttInputChannel.send(MessageBuilder.withPayload("{\"temperature\":27.5,\"humidite\":56.2}").build());
            mqttInputChannel.send(MessageBuilder.withPayload("{\"temperature\":27.5,\"humidite\":56.2,\"idCapteur\":\"%s\"}".formatted(INCONNU)).build());

            assertThat(jdbc.queryForObject("select count(*) from mesure", Integer.class)).isEqualTo(3);
        }

        @Test
        void unMessageMqttPeutDeclencherUneAlerte() {
            jdbc.update("delete from mesure");
            jdbc.update("delete from alerte");
            for (int i = 0; i < 5; i++) {
                String payload = "{\"temperature\":35.00,\"humidite\":55.00,\"dateMesure\":\"2026-03-15T10:0%d:00\",\"idCapteur\":\"%s\"}"
                        .formatted(i, CAPTEUR_B);
                mqttInputChannel.send(MessageBuilder.withPayload(payload).build());
            }
            assertThat(jdbc.queryForObject("select count(*) from alerte where id_capteur = ?", Integer.class, CAPTEUR_B)).isEqualTo(1);
            assertThat(jdbc.queryForObject("select status from lot where id_lot = ?", String.class, LOT_2)).isEqualTo("ALERT");
        }
    }

    // ---------------------------------------------------------------- surface de l'API
    @Nested
    @DisplayName("Routes exposées")
    class Routes {

        @Test
        void laDocumentationOpenApiListeToutesLesRoutesAttendues() throws Exception {
            String json = mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            JsonNode routes = objectMapper.readTree(json).get("paths");
            List<String> attendues = List.of(
                    "/api/lots", "/api/lots/getLot/{idLot}", "/api/lots/allLotsFromAllEntrepots", "/api/lots/getAllQuantity",
                    "/api/lots/getAllMesuresByLotId/{idLot}", "/api/lots/deleteLot/{idLot}", "/api/lots/{idLot}/zone",
                    "/api/lots/{idLot}/status", "/api/lots/oldestLot", "/api/lots/stock-out",
                    "/api/historique/allHistoriquesForLot/{idLot}",
                    "/api/mesures", "/api/mesures/getMesure/{id}", "/api/mesures/deleteMesure/{id}",
                    "/api/capteurs/{capteurId}/mesures",
                    "/api/zone/readings/{zoneId}", "/api/zone/{zoneId}/readings/latest",
                    "/api/entrepots", "/api/entrepots/{entrepotId}/capteurSummary", "/api/entrepots/{idEntrepot}/zones",
                    "/api/exploitation/allExploitations", "/api/alertes",
                    "/api/reporting/global", "/api/reporting/quality", "/api/reporting/stock");
            assertThat(routes.fieldNames()).toIterable().containsExactlyInAnyOrderElementsOf(attendues);
        }
    }

    // ---------------------------------------------------------------- utilitaires
    private void assertJson(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,
                            int expectedStatus, String expectedJson) throws Exception {
        ResultActions result = mockMvc.perform(request);
        MvcResult mvcResult = result.andExpect(status().is(expectedStatus)).andReturn();
        // NON_EXTENSIBLE : aucun champ en trop ni en moins ; l'ordre des tableaux n'est pas significatif
        JSONAssert.assertEquals(expectedJson, mvcResult.getResponse().getContentAsString(), JSONCompareMode.NON_EXTENSIBLE);
    }

    private void zone(UUID id, String nom, UUID entrepot) {
        jdbc.update("insert into zone (id_zone, nom, id_entrepot) values (?, ?, ?)", id, nom, entrepot);
    }

    private void capteur(UUID id, String codeSerie, UUID zone) {
        jdbc.update("insert into capteur (id_capteur, code_serie, id_zone) values (?, ?, ?)", id, codeSerie, zone);
    }

    private void lot(UUID id, String status, int quantite, int caracteristique, String dateStockage, String dateProduction, UUID zone) {
        jdbc.update("""
                        insert into lot (id_lot, status, quantite, caracteristique, date_stockage, date_production, id_exploitation, id_zone)
                        values (?, ?, ?, ?, ?, ?, ?, ?)""",
                id, status, quantite, caracteristique, ts(dateStockage), java.sql.Date.valueOf(LocalDate.parse(dateProduction)), EXPLOITATION, zone);
    }

    private void mesure(UUID id, String temperature, String humidite, String date, UUID capteur) {
        jdbc.update("insert into mesure (id_mesure, temperature, humidite, date_mesure, id_capteur) values (?, ?, ?, ?, ?)",
                id, new java.math.BigDecimal(temperature), new java.math.BigDecimal(humidite), ts(date), capteur);
    }

    private static Timestamp ts(String isoDateTime) {
        return Timestamp.valueOf(LocalDateTime.parse(isoDateTime));
    }
}
