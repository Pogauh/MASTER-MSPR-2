package fr.epsi.mspr2.infrastructure.adapters;

import fr.epsi.mspr2.domain.models.ReportingGlobal;
import fr.epsi.mspr2.domain.models.ReportingQuality;
import fr.epsi.mspr2.domain.models.ReportingStock;
import fr.epsi.mspr2.domain.models.StatusEnum;
import fr.epsi.mspr2.infrastructure.jpa.JpaAlerteRepository;
import fr.epsi.mspr2.infrastructure.jpa.JpaHistoriqueRepository;
import fr.epsi.mspr2.infrastructure.jpa.JpaLotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReportingAdapterTest {

    private JpaLotRepository jpaLotRepository;
    private JpaHistoriqueRepository jpaHistoriqueRepository;
    private JpaAlerteRepository jpaAlerteRepository;
    private ReportingAdapter adapter;

    @BeforeEach
    void setUp() {
        jpaLotRepository = mock(JpaLotRepository.class);
        jpaHistoriqueRepository = mock(JpaHistoriqueRepository.class);
        jpaAlerteRepository = mock(JpaAlerteRepository.class);
        adapter = new ReportingAdapter(jpaLotRepository, jpaHistoriqueRepository, jpaAlerteRepository);
    }

    @Test
    void generateReportingGlobal_shouldReturnCorrectData() {
        when(jpaLotRepository.count()).thenReturn(10L);
        when(jpaLotRepository.countByStatusIn(List.of(StatusEnum.COMPLIANT))).thenReturn(8);
        when(jpaLotRepository.findAverageAgeInDays()).thenReturn(15.0);
        when(jpaHistoriqueRepository.countByDateEmissionAfterAndType(any(), any())).thenReturn(3);

        ReportingGlobal result = adapter.generateReportingGlobal();

        assertThat(result.getTotalLots()).isEqualTo(10);
        assertThat(result.getComplianceRate()).isEqualTo(80.0);
        assertThat(result.getAvgAgeDays()).isEqualTo(15.0);
        assertThat(result.getCountMouvementLast30Days()).isEqualTo(3);
    }

    @Test
    void generateReportingQuality_shouldReturnCorrectData() {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();

        when(jpaLotRepository.count()).thenReturn(10L);
        when(jpaLotRepository.countByStatusIn(List.of(StatusEnum.COMPLIANT))).thenReturn(8);
        when(jpaLotRepository.countByStatusIn(List.of(StatusEnum.ALERT, StatusEnum.EXPIRED))).thenReturn(2);
        when(jpaAlerteRepository.countByDateEmissionBetween(start, end)).thenReturn(5);

        ReportingQuality result = adapter.generateReportingQuality(start, end);

        assertThat(result.getComplianceRate()).isEqualTo(80.0);
        assertThat(result.getNonComplianceCount()).isEqualTo(2);
        assertThat(result.getAlertCount()).isEqualTo(5);
    }

    @Test
    void generateReportingStock_shouldReturnCorrectData() {
        LocalDateTime start = LocalDateTime.now().minusDays(30);
        LocalDateTime end = LocalDateTime.now();
        UUID urgentId = UUID.randomUUID();

        when(jpaLotRepository.count()).thenReturn(5L);
        when(jpaLotRepository.countByStatusIn(List.of(StatusEnum.COMPLIANT))).thenReturn(4);
        when(jpaLotRepository.findAverageAgeInDays()).thenReturn(20.0);
        when(jpaLotRepository.findIdsByDateStockageBetween(any(), any())).thenReturn(List.of(urgentId));

        ReportingStock result = adapter.generateReportingStock(start, end);

        assertThat(result.getTotalLots()).isEqualTo(5);
        assertThat(result.getAvgAgeDays()).isEqualTo(20.0);
        assertThat(result.getLotIdsUrgentFifo()).containsExactly(urgentId);
    }
}

