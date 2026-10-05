package fr.epsi.mspr2.gherkin;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.epsi.mspr2.domain.services.ExpirationService;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Sorties de stock (FIFO, expédition) et expiration des lots. */
public class StockSteps {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    ExpirationService expirationService;
    @Autowired
    ScenarioContext contexte;

    @When("je demande le lot à expédier en premier")
    public void je_demande_le_lot_a_expedier_en_premier() throws Exception {
        contexte.reponse(mockMvc.perform(get("/api/lots/oldestLot")).andReturn());
    }

    @Then("le lot renvoyé est {string}")
    public void le_lot_renvoye_est(String nomLot) throws Exception {
        Assertions.assertEquals(contexte.lot(nomLot).toString(),
                objectMapper.readTree(contexte.reponse().getResponse().getContentAsString()).get("idLot").asText());
    }

    @When("je sors le lot {string} du stock")
    public void je_sors_le_lot_du_stock(String nomLot) throws Exception {
        contexte.reponse(mockMvc.perform(post("/api/lots/stock-out").param("lotId", contexte.lot(nomLot).toString())).andReturn());
    }

    @When("je sors un lot inexistant du stock")
    public void je_sors_un_lot_inexistant_du_stock() throws Exception {
        try {
            contexte.reponse(mockMvc.perform(post("/api/lots/stock-out").param("lotId", java.util.UUID.randomUUID().toString())).andReturn());
        } catch (Exception e) {
            contexte.erreur(e);
        }
    }

    @Then("le lot {string} a une date de sortie")
    public void le_lot_a_une_date_de_sortie(String nomLot) {
        Assertions.assertNotNull(jdbc.queryForObject("select date_sortie from lot where id_lot = ?", java.sql.Timestamp.class, contexte.lot(nomLot)));
    }

    @Then("une expédition est enregistrée dans l'historique du lot {string}")
    public void une_expedition_est_enregistree(String nomLot) {
        List<Map<String, Object>> expeditions = jdbc.queryForList(
                "select * from historique where id_lot = ? and type = 'SHIPPED'", contexte.lot(nomLot));
        Assertions.assertEquals(1, expeditions.size());
        Assertions.assertEquals("Le lot a été expédié.", expeditions.get(0).get("detail"));
    }

    @Then("aucune expédition n'est enregistrée")
    public void aucune_expedition_n_est_enregistree() {
        Assertions.assertEquals(0, jdbc.queryForObject("select count(*) from historique where type = 'SHIPPED'", Integer.class));
    }

    @When("la vérification des lots périmés s'exécute")
    public void la_verification_des_lots_perimes_s_execute() {
        expirationService.expirerLotsPerimes();
    }

    @Then("une alerte d'expiration est enregistrée pour le lot {string}")
    public void une_alerte_d_expiration_est_enregistree(String nomLot) {
        List<Map<String, Object>> alertes = jdbc.queryForList("select * from alerte where type_alerte = 'EXPIRED'");
        Assertions.assertEquals(1, alertes.size());
        Assertions.assertEquals("Le lot " + contexte.lot(nomLot) + " a expiré.", alertes.get(0).get("description"));
    }
}
