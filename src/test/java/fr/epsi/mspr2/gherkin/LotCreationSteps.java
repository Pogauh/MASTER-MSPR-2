package fr.epsi.mspr2.gherkin;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class LotCreationSteps {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    ScenarioContext contexte;

    private final Map<String, Object> demande = new LinkedHashMap<>();
    private String nomZone;

    @Given("un lot à créer avec statut {string}, quantité {int}, caractéristique {int} dans la zone {string}")
    public void un_lot_a_creer(String statut, int quantite, int caracteristique, String nomZone) {
        this.nomZone = nomZone;
        demande.put("status", statut);
        demande.put("quantite", quantite);
        demande.put("caracteristique", caracteristique);
        demande.put("idExploitation", contexte.exploitation("Flamenco"));
        demande.put("idZone", contexte.zone(nomZone));
    }

    @Given("un lot à créer sans exploitation avec statut {string}, quantité {int}, caractéristique {int} dans la zone {string}")
    public void un_lot_a_creer_sans_exploitation(String statut, int quantite, int caracteristique, String nomZone) {
        un_lot_a_creer(statut, quantite, caracteristique, nomZone);
        demande.remove("idExploitation");
    }

    @When("je crée le lot via l'API")
    public void je_cree_le_lot_via_l_api() throws Exception {
        try {
            contexte.reponse(mockMvc.perform(post("/api/lots")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(demande)))
                    .andReturn());
        } catch (Exception e) {
            contexte.erreur(e);
        }
    }

    @Then("le lot est enregistré avec l'exploitation et la zone")
    public void le_lot_est_enregistre_avec_l_exploitation_et_la_zone() throws Exception {
        UUID idLot = UUID.fromString(objectMapper.readTree(contexte.reponse().getResponse().getContentAsString()).get("idLot").asText());

        Map<String, Object> lot = jdbc.queryForMap("select * from lot where id_lot = ?", idLot);
        Assertions.assertEquals(contexte.exploitation("Flamenco"), lot.get("id_exploitation"));
        Assertions.assertEquals(contexte.zone(nomZone), lot.get("id_zone"));
        Assertions.assertEquals(demande.get("status"), lot.get("status"));
        Assertions.assertEquals(demande.get("quantite"), lot.get("quantite"));
        Assertions.assertTrue(((Timestamp) lot.get("date_stockage")).toLocalDateTime().isAfter(LocalDateTime.now().minusMinutes(1)));
    }

    @Then("le lot n'est pas enregistré")
    public void le_lot_n_est_pas_enregistre() {
        Assertions.assertEquals(0, jdbc.queryForObject("select count(*) from lot", Integer.class));
    }
}
