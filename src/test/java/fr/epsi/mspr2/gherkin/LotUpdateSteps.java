package fr.epsi.mspr2.gherkin;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.services.HistoriqueService;
import fr.epsi.mspr2.infrastructure.dto.LotRequestDTO;
import fr.epsi.mspr2.infrastructure.jpa.JpaLotRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

public class LotUpdateSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JpaLotRepository jpaLotRepository;

    @Autowired
    private HistoriqueService historiqueService;

    private UUID idLot;
    private LotRequestDTO requestDTO;
    private MvcResult result;
    private Exception lastException;
    private Lot existingLot;

    @Given("un lot existant avec l'id {string}")
    public void un_lot_existant_avec_l_id(String idLot) {
        this.idLot = UUID.fromString(idLot);
        existingLot = new Lot();
        existingLot.setIdLot(this.idLot);
        when(jpaLotRepository.findByIdWithHistorique(this.idLot)).thenReturn(Optional.of(existingLot));
        when(jpaLotRepository.save(any(Lot.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Given("un lot inexistant avec l'id {string}")
    public void un_lot_inexistant_avec_l_id(String idLot) {
        this.idLot = UUID.fromString(idLot);
        when(jpaLotRepository.findByIdWithHistorique(this.idLot)).thenReturn(Optional.empty());
    }

    @Given("des nouvelles donnees d'emplacement avec id exploitation {string} et id zone {string}")
    public void des_nouvelles_donnees_d_emplacement(String idExploitation, String idZone) {
        requestDTO = new LotRequestDTO();
        requestDTO.idExploitation = UUID.fromString(idExploitation);
        requestDTO.idZone = UUID.fromString(idZone);
    }

    @When("je mets a jour l'emplacement du lot via l'API")
    public void je_mets_a_jour_l_emplacement_du_lot_via_l_api() {
        try {
            result = mockMvc.perform(put("/api/lots/emplacementLot/" + idLot)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)))
                .andReturn();
            lastException = null;
        } catch (Exception ex) {
            lastException = ex;
        }
    }

    @Then("la reponse de mise a jour est {int}")
    public void la_reponse_de_mise_a_jour_est(int expectedStatus) throws Exception {
        Assertions.assertNull(lastException);
        Assertions.assertEquals(expectedStatus, result.getResponse().getStatus());
    }

    @Then("l'historique de transfert est cree")
    public void l_historique_de_transfert_est_cree() {
        verify(historiqueService, times(1)).createTransfereHistoriqueForLot(eq(existingLot), any(Lot.class));
    }

    @Then("l'emplacement du lot est mis a jour")
    public void l_emplacement_du_lot_est_mis_a_jour() {
        ArgumentCaptor<Lot> captor = ArgumentCaptor.forClass(Lot.class);
        verify(jpaLotRepository, times(1)).save(captor.capture());
        Lot savedLot = captor.getValue();
        
        Assertions.assertEquals(requestDTO.idZone, savedLot.getZone().getIdZone());
    }
}
