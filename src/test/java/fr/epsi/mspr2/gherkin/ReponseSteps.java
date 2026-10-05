package fr.epsi.mspr2.gherkin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Then;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

/** Vérifications portant sur la dernière réponse HTTP (ou la dernière erreur) du scénario. */
public class ReponseSteps {

    @Autowired
    ScenarioContext contexte;
    @Autowired
    ObjectMapper objectMapper;

    @Then("la réponse est {int}")
    public void la_reponse_est(int statut) {
        Assertions.assertNull(contexte.erreur(), "Une erreur a été levée : " + contexte.erreur());
        Assertions.assertEquals(statut, contexte.reponse().getResponse().getStatus());
    }

    @Then("la réponse est {int} et un id de lot est retourné")
    public void la_reponse_est_et_un_id_de_lot_est_retourne(int statut) throws Exception {
        la_reponse_est(statut);
        JsonNode corps = objectMapper.readTree(contexte.reponse().getResponse().getContentAsString());
        Assertions.assertTrue(corps.hasNonNull("idLot"));
    }

    @Then("le message d'erreur est {string}")
    public void le_message_d_erreur_est(String message) {
        Assertions.assertEquals(message, contexte.reponse().getResponse().getErrorMessage());
    }

    @Then("une erreur est levée avec le message {string}")
    public void une_erreur_est_levee_avec_le_message(String message) {
        Assertions.assertNotNull(contexte.erreur(), "Aucune erreur n'a été levée");
        Assertions.assertTrue(contexte.erreur().getMessage().contains(message), contexte.erreur().getMessage());
    }
}
