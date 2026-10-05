package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.api.mapper.ReportingMapperImpl;
import fr.epsi.mspr2.domain.models.ReportingGlobal;
import fr.epsi.mspr2.domain.models.ReportingQuality;
import fr.epsi.mspr2.domain.models.ReportingStock;
import fr.epsi.mspr2.domain.services.ReportingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportingController.class)
@Import(ReportingMapperImpl.class)
class ReportingControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockBean
    ReportingService service;

    @Test
    void global() throws Exception {
        when(service.getReportingGlobal()).thenReturn(new ReportingGlobal(3, 10.5, 66.5, 2));

        mockMvc.perform(get("/api/reporting/global"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"totalLots":3,"avgAgeDays":10.5,"complianceRate":66.5,"countMouvementLast30Days":2}
                        """, true));
    }

    @Test
    void quality() throws Exception {
        LocalDateTime debut = LocalDateTime.of(2025, 2, 1, 0, 0);
        LocalDateTime fin = LocalDateTime.of(2025, 2, 5, 0, 0);
        when(service.getReportingQuality(debut, fin)).thenReturn(new ReportingQuality(4, 80.0, 1));

        mockMvc.perform(get("/api/reporting/quality").param("startDate", "2025-02-01T00:00:00").param("endDate", "2025-02-05T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"alertCount\":4,\"complianceRate\":80.0,\"nonComplianceCount\":1}", true));
    }

    @Test
    void stock_leRapportPorteSurLeStockCourant() throws Exception {
        UUID urgent = UUID.fromString("f1f1f1f1-f1f1-f1f1-f1f1-f1f1f1f1f1f1");
        when(service.getReportingStock()).thenReturn(new ReportingStock(3, 12.0, 100.0, List.of(urgent)));

        mockMvc.perform(get("/api/reporting/stock").param("startDate", "2025-02-01T00:00:00").param("endDate", "2025-02-05T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"totalLots":3,"avgAgeDays":12.0,"complianceRate":100.0,"lotIdsUrgentFifo":["%s"]}
                        """.formatted(urgent), true));

        verify(service).getReportingStock();
    }

    @Test
    void qualityEtStockExigentLesDeuxDates() throws Exception {
        mockMvc.perform(get("/api/reporting/quality")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/reporting/quality").param("startDate", "2025-02-01T00:00:00")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/reporting/stock")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/reporting/stock").param("endDate", "2025-02-05T00:00:00")).andExpect(status().isBadRequest());
    }
}
