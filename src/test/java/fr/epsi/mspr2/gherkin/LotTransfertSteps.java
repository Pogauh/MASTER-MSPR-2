package fr.epsi.mspr2.gherkin;

import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

public class LotTransfertSteps {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    ScenarioContext contexte;

    @When("je déplace le lot {string} vers la zone {string}")
    public void je_deplace_le_lot_vers_la_zone(String nomLot, String nomZone) throws Exception {
        deplacer(contexte.lot(nomLot), contexte.zone(nomZone));
    }

    @When("je déplace un lot inexistant vers la zone {string}")
    public void je_deplace_un_lot_inexistant_vers_la_zone(String nomZone) throws Exception {
        deplacer(UUID.randomUUID(), contexte.zone(nomZone));
    }

    @Then("un transfert de la zone {string} vers la zone {string} est enregistré dans l'historique du lot {string}")
    public void un_transfert_est_enregistre(String origine, String destination, String nomLot) {
        List<Map<String, Object>> transferts = jdbc.queryForList(
                "select * from historique where id_lot = ? and type = 'TRANSFERT'", contexte.lot(nomLot));
        Assertions.assertEquals(1, transferts.size());
        Assertions.assertEquals(contexte.zone(origine), transferts.get(0).get("zone_precedente"));
        Assertions.assertEquals(contexte.zone(destination), transferts.get(0).get("zone_actuelle"));
    }

    @Then("aucun transfert n'est enregistré")
    public void aucun_transfert_n_est_enregistre() {
        Assertions.assertEquals(0, jdbc.queryForObject("select count(*) from historique where type = 'TRANSFERT'", Integer.class));
    }

    private void deplacer(UUID idLot, UUID idZone) throws Exception {
        try {
            contexte.reponse(mockMvc.perform(put("/api/lots/{id}/zone", idLot)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"idZone\":\"" + idZone + "\"}"))
                    .andReturn());
        } catch (Exception e) {
            contexte.erreur(e);
        }
    }
}
