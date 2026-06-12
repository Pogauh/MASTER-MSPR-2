package fr.epsi.mspr2.gherkin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.infrastructure.dto.LotRequestDTO;
import fr.epsi.mspr2.infrastructure.jpa.JpaHistoriqueRepository;
import fr.epsi.mspr2.infrastructure.jpa.JpaLotRepository;
import fr.epsi.mspr2.infrastructure.jpa.JpaMesureRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class LotCreationSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JpaLotRepository jpaLotRepository;

    @Autowired
    private JpaHistoriqueRepository historiqueJpaRepository;

    @Autowired
    private JpaMesureRepository jpaMesureRepository;

    private UUID exploitationId;
    private UUID zoneId;
    private String status;
    private Integer quantite;
    private Integer caracteristique;
    private MvcResult result;
    private Exception lastException;

    @Before
    public void setUp() {
        when(jpaLotRepository.save(any(Lot.class))).thenAnswer(invocation -> {
            Lot lot = invocation.getArgument(0);
            lot.setIdLot(UUID.randomUUID());
            return lot;
        });
    }

    @Given("un lot a creer avec statut {string}, quantite {int}, caracteristique {int}, id exploitation {string} et id zone {string}")
    public void un_lot_a_creer(String status, Integer quantite, Integer caracteristique, String idExploitation, String idZone) {
        this.status = status;
        this.quantite = quantite;
        this.caracteristique = caracteristique;
        this.exploitationId = UUID.fromString(idExploitation);
        this.zoneId = UUID.fromString(idZone);
    }

    @When("je cree le lot via l'API")
    public void je_cree_le_lot_via_l_api() throws Exception {
        LotRequestDTO request = new LotRequestDTO();
        request.status = status;
        request.quantite = quantite;
        request.caracteristique = caracteristique;
        request.idExploitation = exploitationId;
        request.idZone = zoneId;

        try {
            result = mockMvc.perform(post("/api/lots")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andReturn();
            lastException = null;
        } catch (Exception ex) {
            lastException = ex;
        }
    }

    @Then("la reponse est {int} et un id de lot est retourne")
    public void la_reponse_est_et_un_id_de_lot_est_retourne(int statusCode) throws Exception {
        Assertions.assertNull(lastException);
        Assertions.assertEquals(statusCode, result.getResponse().getStatus());
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        Assertions.assertTrue(body.hasNonNull("idLot"));
    }

    @Then("le lot est persiste avec l'exploitation et la zone")
    public void le_lot_est_persiste_avec_l_exploitation_et_la_zone() {
        ArgumentCaptor<Lot> captor = ArgumentCaptor.forClass(Lot.class);
        verify(jpaLotRepository).save(captor.capture());
        Lot saved = captor.getValue();
        Assertions.assertEquals(exploitationId, saved.getExploitation().getIdExploitation());
        Assertions.assertEquals(zoneId, saved.getZone().getIdZone());
        Assertions.assertNotNull(saved.getDateStockage());
    }

    @Given("un lot a creer sans exploitation avec statut {string}, quantite {int}, caracteristique {int} et id zone {string}")
    public void un_lot_a_creer_sans_exploitation(String status, Integer quantite, Integer caracteristique, String idZone) {
        this.status = status;
        this.quantite = quantite;
        this.caracteristique = caracteristique;
        this.exploitationId = null;
        this.zoneId = UUID.fromString(idZone);
    }

    @Then("la reponse est {int}")
    public void la_reponse_est(int statusCode) {
        Assertions.assertNull(lastException);
        Assertions.assertEquals(statusCode, result.getResponse().getStatus());
    }

    @Then("une erreur est levee avec le message {string}")
    public void une_erreur_est_levee_avec_le_message(String message) {
        Assertions.assertNotNull(lastException);
        Assertions.assertTrue(lastException.getMessage().contains(message));
    }

    @Then("le lot n'est pas persiste")
    public void le_lot_n_est_pas_persiste() {
        verify(jpaLotRepository, never()).save(any(Lot.class));
    }
}
