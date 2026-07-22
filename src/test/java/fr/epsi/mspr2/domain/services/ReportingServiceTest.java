package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.ReportingGlobal;
import fr.epsi.mspr2.domain.models.ReportingQuality;
import fr.epsi.mspr2.domain.models.ReportingStock;
import fr.epsi.mspr2.domain.ports.ReportingPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ReportingServiceTest {

    private ReportingPort reportingPort;
    private ReportingService service;

    @BeforeEach
    void setUp() {
        reportingPort = mock(ReportingPort.class);
        service = new ReportingService(reportingPort);
    }

    @Test
    void getReportingGlobal_shouldDelegateToPort() {
        ReportingGlobal global = new ReportingGlobal();
        global.setTotalLots(5);
        when(reportingPort.generateReportingGlobal()).thenReturn(global);

        ReportingGlobal result = service.getReportingGlobal();

        assertThat(result.getTotalLots()).isEqualTo(5);
    }

    @Test
    void getReportingQuality_shouldDelegateToPort() {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        ReportingQuality quality = new ReportingQuality();
        quality.setAlertCount(3);

        when(reportingPort.generateReportingQuality(start, end)).thenReturn(quality);

        ReportingQuality result = service.getReportingQuality(start, end);

        assertThat(result.getAlertCount()).isEqualTo(3);
    }

    @Test
    void getReportingStock_shouldDelegateToPort() {
        LocalDateTime start = LocalDateTime.now().minusDays(30);
        LocalDateTime end = LocalDateTime.now();
        ReportingStock stock = new ReportingStock();
        stock.setTotalLots(10);

        when(reportingPort.generateReportingStock(start, end)).thenReturn(stock);

        ReportingStock result = service.getReportingStock(start, end);

        assertThat(result.getTotalLots()).isEqualTo(10);
    }
}



