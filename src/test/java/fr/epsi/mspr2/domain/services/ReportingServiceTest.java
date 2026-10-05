package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.ReportingGlobal;
import fr.epsi.mspr2.domain.models.ReportingQuality;
import fr.epsi.mspr2.domain.models.ReportingStock;
import fr.epsi.mspr2.domain.models.StatusEnum;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.domain.ports.AlertePort;
import fr.epsi.mspr2.domain.ports.HistoriquePort;
import fr.epsi.mspr2.domain.ports.LotPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;
import java.util.UUID;

import static fr.epsi.mspr2.domain.support.Fixtures.CLOCK;
import static fr.epsi.mspr2.domain.support.Fixtures.MAINTENANT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportingServiceTest {

    @Mock
    LotPort lotPort;
    @Mock
    HistoriquePort historiquePort;
    @Mock
    AlertePort alertePort;

    ReportingService service;

    @BeforeEach
    void setUp() {
        service = new ReportingService(lotPort, historiquePort, alertePort, CLOCK);
    }

    @Test
    void getReportingGlobal_totalAgeMoyenTauxDeConformiteEtMouvementsDes30DerniersJours() {
        when(lotPort.count()).thenReturn(8L);
        when(lotPort.countByStatusIn(List.of(StatusEnum.COMPLIANT))).thenReturn(6L);
        when(lotPort.findAverageAgeInDays()).thenReturn(OptionalDouble.of(42.5));
        when(historiquePort.countByTypeAfter(TypeHistoriqueEnum.TRANSFERT, MAINTENANT.minusDays(30))).thenReturn(4L);

        assertThat(service.getReportingGlobal()).isEqualTo(new ReportingGlobal(8, 42.5, 75.0, 4));
    }

    @Test
    void getReportingGlobal_sansLot_toutEstANul() {
        when(lotPort.count()).thenReturn(0L);
        when(lotPort.findAverageAgeInDays()).thenReturn(OptionalDouble.empty());
        when(historiquePort.countByTypeAfter(TypeHistoriqueEnum.TRANSFERT, MAINTENANT.minusDays(30))).thenReturn(0L);

        assertThat(service.getReportingGlobal()).isEqualTo(new ReportingGlobal(0, 0.0, 0.0, 0));
        verify(lotPort, never()).countByStatusIn(List.of(StatusEnum.COMPLIANT));
    }

    @Test
    void getReportingQuality_alertesDeLaPeriodeEtLotsNonConformes() {
        LocalDateTime debut = MAINTENANT.minusDays(7);
        when(lotPort.count()).thenReturn(3L);
        when(lotPort.countByStatusIn(List.of(StatusEnum.COMPLIANT))).thenReturn(2L);
        when(lotPort.countByStatusIn(List.of(StatusEnum.ALERT, StatusEnum.EXPIRED))).thenReturn(1L);
        when(alertePort.countByDateEmissionBetween(debut, MAINTENANT)).thenReturn(5);

        ReportingQuality qualite = service.getReportingQuality(debut, MAINTENANT);

        assertThat(qualite.alertCount()).isEqualTo(5);
        assertThat(qualite.nonComplianceCount()).isEqualTo(1);
        assertThat(qualite.complianceRate()).isEqualTo(2 / 3.0 * 100);
    }

    @Test
    void getReportingQuality_sansLot_leTauxEstNul() {
        when(lotPort.count()).thenReturn(0L);
        when(lotPort.countByStatusIn(List.of(StatusEnum.ALERT, StatusEnum.EXPIRED))).thenReturn(0L);
        when(alertePort.countByDateEmissionBetween(MAINTENANT, MAINTENANT)).thenReturn(0);

        assertThat(service.getReportingQuality(MAINTENANT, MAINTENANT)).isEqualTo(new ReportingQuality(0, 0.0, 0));
    }

    @Test
    void getReportingStock_lotsStockesDepuis9A12MoisAEcoulerEnPriorite() {
        UUID urgent = UUID.randomUUID();
        when(lotPort.count()).thenReturn(4L);
        when(lotPort.countByStatusIn(List.of(StatusEnum.COMPLIANT))).thenReturn(3L);
        when(lotPort.findAverageAgeInDays()).thenReturn(OptionalDouble.of(120.0));
        when(lotPort.findIdsByDateStockageBetween(MAINTENANT.minusYears(1), MAINTENANT.minusMonths(9))).thenReturn(List.of(urgent));

        assertThat(service.getReportingStock()).isEqualTo(new ReportingStock(4, 120.0, 75.0, List.of(urgent)));
    }
}
