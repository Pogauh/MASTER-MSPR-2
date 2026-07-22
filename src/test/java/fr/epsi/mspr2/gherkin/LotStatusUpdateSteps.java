package fr.epsi.mspr2.gherkin;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.epsi.mspr2.infrastructure.dto.LotStatusRequestDTO;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

public class LotStatusUpdateSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScenarioContext scenarioContext;

    private MvcResult result;

    // Note: les steps "un lot existant/inexistant avec l'id" sont définis dans LotUpdateSteps
    // et alimentent scenarioContext.currentLotId

    @Given("un nouveau statut {string}")
    public void un_nouveau_statut(String statut) {
        scenarioContext.nouveauStatut = statut;
    }

    @When("je mets a jour le statut du lot via l'API")
    public void je_mets_a_jour_le_statut_via_l_api() throws Exception {
        LotStatusRequestDTO requestDTO = new LotStatusRequestDTO();
        requestDTO.status = scenarioContext.nouveauStatut;

        result = mockMvc.perform(put("/api/lots/{id}/status", scenarioContext.currentLotId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andReturn();
    }

    @Then("la reponse de mise a jour de statut est {int}")
    public void la_reponse_de_mise_a_jour_de_statut_est(int statusCode) {
        Assertions.assertEquals(statusCode, result.getResponse().getStatus());
    }
}



