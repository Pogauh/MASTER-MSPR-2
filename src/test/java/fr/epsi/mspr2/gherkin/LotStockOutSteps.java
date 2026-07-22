package fr.epsi.mspr2.gherkin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.epsi.mspr2.domain.models.*;
import fr.epsi.mspr2.infrastructure.jpa.JpaHistoriqueRepository;
import fr.epsi.mspr2.infrastructure.jpa.JpaLotRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class LotStockOutSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JpaLotRepository jpaLotRepository;

    @Autowired
    private JpaHistoriqueRepository historiqueJpaRepository;

    @Autowired
    private ScenarioContext scenarioContext;

    private UUID lotId;
    private MvcResult result;
    private Lot existingLot;

    @Given("un lot pour stock-out avec l'id {string}")
    public void un_lot_pour_stock_out(String id) {
        lotId = UUID.fromString(id);
        existingLot = new Lot();
        existingLot.setIdLot(lotId);
        existingLot.setStatus(StatusEnum.COMPLIANT);
        Zone zone = new Zone();
        zone.setIdZone(UUID.randomUUID());
        existingLot.setZone(zone);
        existingLot.setDateStockage(LocalDateTime.now().minusDays(5));

        when(jpaLotRepository.findByIdWithHistorique(lotId)).thenReturn(Optional.of(existingLot));
        when(jpaLotRepository.save(any(Lot.class))).thenAnswer(inv -> inv.getArgument(0));

        Historique savedHistorique = new Historique();
        savedHistorique.setIdHistorique(UUID.randomUUID());
        savedHistorique.setType(TypeHistoriqueEnum.SHIPPED);
        savedHistorique.setLot(existingLot);
        savedHistorique.setDateEmission(LocalDateTime.now());
        when(historiqueJpaRepository.save(any(Historique.class))).thenReturn(savedHistorique);
    }

    @When("je fais le stock-out du lot via l'API")
    public void je_fais_le_stock_out() throws Exception {
        result = mockMvc.perform(post("/api/lots/stock-out")
                .param("lotId", lotId.toString()))
                .andReturn();
    }

    @Then("la reponse de stock-out est {int} et un id d'historique est retourne")
    public void la_reponse_de_stock_out_est_et_historique_retourne(int statusCode) throws Exception {
        Assertions.assertEquals(statusCode, result.getResponse().getStatus());
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        Assertions.assertTrue(body.hasNonNull("idHistorique"));
    }

    @Then("le lot est marque comme expedie")
    public void le_lot_est_marque_comme_expedie() {
        ArgumentCaptor<Lot> captor = ArgumentCaptor.forClass(Lot.class);
        verify(jpaLotRepository, atLeastOnce()).save(captor.capture());
        boolean anyShipped = captor.getAllValues().stream()
                .anyMatch(l -> StatusEnum.SHIPPED.equals(l.getStatus()));
        Assertions.assertTrue(anyShipped, "Le lot devrait être marqué comme SHIPPED");
    }
}


