package fr.epsi.mspr2.gherkin;

import fr.epsi.mspr2.domain.services.AlerteService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class AlerteSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlerteService alerteService;

    private UUID entrepotId;
    private MvcResult result;

    @Given("un entrepot avec l'id {string}")
    public void un_entrepot_avec_l_id(String id) {
        entrepotId = UUID.fromString(id);
        when(alerteService.getAlerteByEntrepot(eq(entrepotId), any(), any()))
                .thenReturn(List.of());
    }

    @When("je consulte les alertes de l'entrepot entre {string} et {string}")
    public void je_consulte_les_alertes_avec_dates(String startDate, String endDate) throws Exception {
        result = mockMvc.perform(get("/api/alertes")
                .param("idEntrepot", entrepotId.toString())
                .param("startDate", startDate)
                .param("endDate", endDate))
                .andReturn();
    }

    @When("je consulte les alertes de l'entrepot sans dates")
    public void je_consulte_les_alertes_sans_dates() throws Exception {
        result = mockMvc.perform(get("/api/alertes")
                .param("idEntrepot", entrepotId.toString()))
                .andReturn();
    }

    @Then("la reponse des alertes est {int} et la liste est retournee")
    public void la_reponse_des_alertes_est(int statusCode) throws Exception {
        Assertions.assertEquals(statusCode, result.getResponse().getStatus());
        String body = result.getResponse().getContentAsString();
        Assertions.assertTrue(body.startsWith("["), "La réponse devrait être un tableau JSON");
    }
}

