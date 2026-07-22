package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.domain.models.ReportingGlobal;
import fr.epsi.mspr2.domain.models.ReportingQuality;
import fr.epsi.mspr2.domain.models.ReportingStock;
import fr.epsi.mspr2.domain.services.ReportingService;
import fr.epsi.mspr2.infrastructure.dto.ReportingGlobalDto;
import fr.epsi.mspr2.infrastructure.dto.ReportingQualityDto;
import fr.epsi.mspr2.infrastructure.dto.ReportingStockDto;
import fr.epsi.mspr2.infrastructure.mapper.ReportingMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReportingControllerTest {

    private MockMvc mockMvc;
    private ReportingService reportingService;
    private ReportingMapper reportingMapper;

    @BeforeEach
    void setUp() {
        reportingService = mock(ReportingService.class);
        reportingMapper = mock(ReportingMapper.class);
        ReportingController controller = new ReportingController(reportingService, reportingMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getGlobalReport_shouldReturn200() throws Exception {
        ReportingGlobal global = new ReportingGlobal();
        global.setTotalLots(5);
        ReportingGlobalDto dto = new ReportingGlobalDto();
        dto.totalLots = 5;

        when(reportingService.getReportingGlobal()).thenReturn(global);
        when(reportingMapper.toDto(global)).thenReturn(dto);

        mockMvc.perform(get("/api/reporting/global"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLots").value(5));
    }

    @Test
    void getQualityReport_shouldReturn200WithDates() throws Exception {
        ReportingQuality quality = new ReportingQuality();
        ReportingQualityDto dto = new ReportingQualityDto();
        dto.alertCount = 2;

        when(reportingService.getReportingQuality(any(), any())).thenReturn(quality);
        when(reportingMapper.toReportingQualityDto(quality)).thenReturn(dto);

        mockMvc.perform(get("/api/reporting/quality")
                        .param("startDate", "2026-01-01T00:00:00")
                        .param("endDate", "2026-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertCount").value(2));
    }

    @Test
    void getStockReport_shouldReturn200WithDates() throws Exception {
        ReportingStock stock = new ReportingStock();
        ReportingStockDto dto = new ReportingStockDto();
        dto.totalLots = 10;

        when(reportingService.getReportingStock(any(), any())).thenReturn(stock);
        when(reportingMapper.toReportingStockDto(stock)).thenReturn(dto);

        mockMvc.perform(get("/api/reporting/stock")
                        .param("startDate", "2026-01-01T00:00:00")
                        .param("endDate", "2026-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLots").value(10));
    }
}


