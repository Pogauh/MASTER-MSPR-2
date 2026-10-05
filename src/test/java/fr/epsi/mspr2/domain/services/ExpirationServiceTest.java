package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Alerte;
import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.StatusEnum;
import fr.epsi.mspr2.domain.models.TypeAlerteEnum;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.domain.models.Zone;
import fr.epsi.mspr2.domain.ports.AlertePort;
import fr.epsi.mspr2.domain.ports.HistoriquePort;
import fr.epsi.mspr2.domain.ports.LotPort;
import fr.epsi.mspr2.domain.ports.NotificationPort;
import fr.epsi.mspr2.domain.support.PassThroughTransactionPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static fr.epsi.mspr2.domain.support.Fixtures.CLOCK;
import static fr.epsi.mspr2.domain.support.Fixtures.MAINTENANT;
import static fr.epsi.mspr2.domain.support.Fixtures.lot;
import static fr.epsi.mspr2.domain.support.Fixtures.zone;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpirationServiceTest {

    private static final LocalDate AUJOURDHUI = LocalDate.of(2026, 3, 15);
    private static final LocalDate LIMITE = AUJOURDHUI.minusYears(1);

    @Mock
    LotPort lotPort;
    @Mock
    AlertePort alertePort;
    @Mock
    HistoriquePort historiquePort;
    @Mock
    NotificationPort notificationPort;

    final PassThroughTransactionPort transaction = new PassThroughTransactionPort();
    ExpirationService service;

    @BeforeEach
    void setUp() {
        service = new ExpirationService(lotPort, alertePort, historiquePort, notificationPort, transaction, CLOCK);
    }

    private Lot lotProduitLe(LocalDate dateProduction, StatusEnum status) {
        Lot lot = lot(zone("A"));
        lot.setDateProduction(dateProduction);
        lot.setStatus(status);
        return lot;
    }

    @Test
    void aucunLotPerime_neFaitRien() {
        when(lotPort.findByDateProductionBefore(LIMITE)).thenReturn(List.of());

        assertThat(service.expirerLotsPerimes()).isZero();

        verify(alertePort, never()).save(any());
        verifyNoMoreInteractions(historiquePort, notificationPort);
        assertThat(transaction.executions()).isZero();
    }

    @Test
    void unLotPerime_expireEtDeclencheUneAlerte() {
        Lot perime = lotProduitLe(LIMITE.minusDays(1), StatusEnum.COMPLIANT);
        when(lotPort.findByDateProductionBefore(LIMITE)).thenReturn(List.of(perime));
        UUID idAlerte = UUID.randomUUID();
        when(alertePort.save(any(Alerte.class))).thenAnswer(invocation -> {
            Alerte alerte = invocation.getArgument(0);
            alerte.setIdAlerte(idAlerte);
            return alerte;
        });

        assertThat(service.expirerLotsPerimes()).isEqualTo(1);

        assertThat(perime.getStatus()).isEqualTo(StatusEnum.EXPIRED);
        verify(lotPort).save(perime);

        ArgumentCaptor<Alerte> alerte = ArgumentCaptor.forClass(Alerte.class);
        verify(alertePort).save(alerte.capture());
        assertThat(alerte.getValue().getTypeAlerte()).isEqualTo(TypeAlerteEnum.EXPIRED);
        assertThat(alerte.getValue().getDescription()).isEqualTo("Le lot " + perime.getIdLot() + " a expiré.");
        assertThat(alerte.getValue().getDateEmission()).isEqualTo(MAINTENANT);
        assertThat(alerte.getValue().getCapteur()).isNull();

        ArgumentCaptor<Historique> historique = ArgumentCaptor.forClass(Historique.class);
        verify(historiquePort).save(historique.capture());
        assertThat(historique.getValue().getType()).isEqualTo(TypeHistoriqueEnum.ALERTE);
        assertThat(historique.getValue().getDetail()).isEqualTo("Lot expiré");
        assertThat(historique.getValue().getIdLot()).isEqualTo(perime.getIdLot());
        assertThat(historique.getValue().getZonePrecedente()).isEqualTo(perime.getZone().getIdZone());
        assertThat(historique.getValue().getZoneActuelle()).isEqualTo(perime.getZone().getIdZone());

        verify(notificationPort).notifierAlerte(alerte.getValue());
    }

    @Test
    void chaqueLotExpireDansSaPropreTransaction() {
        Zone zone = zone("A");
        Lot premier = lotProduitLe(LIMITE.minusYears(1), StatusEnum.COMPLIANT);
        Lot second = lotProduitLe(LIMITE.minusDays(10), StatusEnum.BLOCKED);
        premier.setZone(zone);
        second.setZone(zone);
        when(lotPort.findByDateProductionBefore(LIMITE)).thenReturn(List.of(premier, second));
        when(alertePort.save(any(Alerte.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.expirerLotsPerimes()).isEqualTo(2);

        assertThat(transaction.executions()).isEqualTo(2);
        assertThat(List.of(premier, second)).allSatisfy(lot -> assertThat(lot.getStatus()).isEqualTo(StatusEnum.EXPIRED));
        verify(notificationPort, times(2)).notifierAlerte(any(Alerte.class));
    }

    @Test
    void unLotDejaExpire_nEstPasTraiteDeNouveau() {
        Lot dejaExpire = lotProduitLe(LIMITE.minusYears(1), StatusEnum.EXPIRED);
        when(lotPort.findByDateProductionBefore(LIMITE)).thenReturn(List.of(dejaExpire));

        assertThat(service.expirerLotsPerimes()).isZero();

        verify(alertePort, never()).save(any());
        verify(lotPort, never()).save(any());
        verifyNoMoreInteractions(historiquePort, notificationPort);
    }

    @Test
    void unLotSansDateDeProductionNExpirePas() {
        Lot sansDate = lotProduitLe(null, StatusEnum.COMPLIANT);
        when(lotPort.findByDateProductionBefore(LIMITE)).thenReturn(List.of(sansDate));

        assertThat(service.expirerLotsPerimes()).isZero();

        assertThat(sansDate.getStatus()).isEqualTo(StatusEnum.COMPLIANT);
    }

    @Test
    void uneErreurDEnregistrementInterrompLeTraitement() {
        Lot perime = lotProduitLe(LIMITE.minusDays(1), StatusEnum.COMPLIANT);
        when(lotPort.findByDateProductionBefore(LIMITE)).thenReturn(List.of(perime));
        when(alertePort.save(any(Alerte.class))).thenThrow(new IllegalStateException("base indisponible"));

        assertThatThrownBy(() -> service.expirerLotsPerimes()).isInstanceOf(IllegalStateException.class);

        verify(notificationPort, never()).notifierAlerte(any());
    }
}
