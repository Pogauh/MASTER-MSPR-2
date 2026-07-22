package fr.epsi.mspr2.gherkin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.infrastructure.dto.MesureRequestDTO;
import fr.epsi.mspr2.infrastructure.jpa.JpaMesureRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class MesureSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JpaMesureRepository jpaMesureRepository;

    private MvcResult result;
    private MesureRequestDTO request;
    private UUID mesureId;

    @Before
    public void setUp() {
        when(jpaMesureRepository.save(any(Mesure.class))).thenAnswer(inv -> {
            Mesure m = inv.getArgument(0);
            m.setIdMesure(UUID.randomUUID());
            return m;
        });
    }

    @Given("une mesure a creer avec temperature {double}, humidite {double} et id capteur {string}")
    public void une_mesure_a_creer(Double temp, Double humi, String idCapteur) {
        request = new MesureRequestDTO();
        request.temperature = BigDecimal.valueOf(temp);
        request.humidite = BigDecimal.valueOf(humi);
        request.idCapteur = UUID.fromString(idCapteur);
    }

    @Given("une mesure a creer avec temperature {double}, humidite {double} et sans id capteur")
    public void une_mesure_sans_id_capteur(Double temp, Double humi) {
        request = new MesureRequestDTO();
        request.temperature = BigDecimal.valueOf(temp);
        request.humidite = BigDecimal.valueOf(humi);
        request.idCapteur = null;
    }

    @When("je cree la mesure via l'API")
    public void je_cree_la_mesure_via_l_api() throws Exception {
        // Mock minimal : save de la mesure retourne la mesure avec un id capteur
        Capteur capteur = new Capteur();
        capteur.setIdCapteur(request.idCapteur != null ? request.idCapteur : UUID.randomUUID());
        when(jpaMesureRepository.findTop5ByCapteurIdCapteurOrderByDateMesureDesc(any())).thenReturn(java.util.List.of());

        result = mockMvc.perform(post("/api/mesures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn();
    }

    @Then("la reponse de mesure est {int} et un id de mesure est retourne")
    public void la_reponse_de_mesure_est_et_un_id_est_retourne(int statusCode) throws Exception {
        Assertions.assertEquals(statusCode, result.getResponse().getStatus());
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        Assertions.assertTrue(body.hasNonNull("idMesure"));
        mesureId = UUID.fromString(body.get("idMesure").asText());
    }

    @Then("la reponse de mesure est une erreur serveur")
    public void la_reponse_est_une_erreur_serveur() {
        int status = result.getResponse().getStatus();
        Assertions.assertTrue(status >= 400, "Expected error status, got: " + status);
    }

    @Given("une mesure existante avec l'id {string}")
    public void une_mesure_existante(String id) {
        mesureId = UUID.fromString(id);
        Mesure m = new Mesure();
        m.setIdMesure(mesureId);
        Capteur c = new Capteur();
        c.setIdCapteur(UUID.randomUUID());
        m.setCapteur(c);
        m.setTemperature(new BigDecimal("20.0"));
        m.setHumidite(new BigDecimal("50.0"));
        when(jpaMesureRepository.findById(mesureId)).thenReturn(Optional.of(m));
    }

    @Given("une mesure inexistante avec l'id {string}")
    public void une_mesure_inexistante(String id) {
        mesureId = UUID.fromString(id);
        when(jpaMesureRepository.findById(mesureId)).thenReturn(Optional.empty());
    }

    @When("je supprime la mesure via l'API")
    public void je_supprime_la_mesure_via_l_api() throws Exception {
        result = mockMvc.perform(delete("/api/mesures/deleteMesure/{id}", mesureId))
                .andReturn();
    }

    @Then("la reponse de suppression de mesure est {int}")
    public void la_reponse_de_suppression_de_mesure_est(int statusCode) {
        Assertions.assertEquals(statusCode, result.getResponse().getStatus());
    }
}

