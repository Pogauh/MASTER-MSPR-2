package fr.epsi.mspr2.gherkin;

import fr.epsi.mspr2.domain.models.ReportingGlobal;
import fr.epsi.mspr2.domain.models.ReportingQuality;
import fr.epsi.mspr2.domain.models.ReportingStock;
import fr.epsi.mspr2.domain.services.ReportingService;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class ReportingSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReportingService reportingService;

    private MvcResult result;

    @When("je consulte le reporting global via l'API")
    public void je_consulte_le_reporting_global() throws Exception {
        ReportingGlobal global = new ReportingGlobal();
        global.setTotalLots(5);
        global.setComplianceRate(80.0);
        when(reportingService.getReportingGlobal()).thenReturn(global);

        result = mockMvc.perform(get("/api/reporting/global"))
                .andReturn();
    }

    @Then("la reponse de reporting global est {int}")
    public void la_reponse_de_reporting_global_est(int statusCode) {
        Assertions.assertEquals(statusCode, result.getResponse().getStatus());
    }

    @When("je consulte le reporting qualite entre {string} et {string}")
    public void je_consulte_le_reporting_qualite(String startDate, String endDate) throws Exception {
        ReportingQuality quality = new ReportingQuality();
        quality.setAlertCount(2);
        quality.setComplianceRate(75.0);
        when(reportingService.getReportingQuality(any(), any())).thenReturn(quality);

        result = mockMvc.perform(get("/api/reporting/quality")
                .param("startDate", startDate)
                .param("endDate", endDate))
                .andReturn();
    }

    @Then("la reponse de reporting qualite est {int}")
    public void la_reponse_de_reporting_qualite_est(int statusCode) {
        Assertions.assertEquals(statusCode, result.getResponse().getStatus());
    }

    @When("je consulte le reporting stock entre {string} et {string}")
    public void je_consulte_le_reporting_stock(String startDate, String endDate) throws Exception {
        ReportingStock stock = new ReportingStock();
        stock.setTotalLots(10);
        stock.setLotIdsUrgentFifo(List.of());
        when(reportingService.getReportingStock(any(), any())).thenReturn(stock);

        result = mockMvc.perform(get("/api/reporting/stock")
                .param("startDate", startDate)
                .param("endDate", endDate))
                .andReturn();
    }

    @Then("la reponse de reporting stock est {int}")
    public void la_reponse_de_reporting_stock_est(int statusCode) {
        Assertions.assertEquals(statusCode, result.getResponse().getStatus());
    }
}

