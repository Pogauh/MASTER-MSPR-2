package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.domain.models.CapteurSummary;
import fr.epsi.mspr2.domain.models.Entrepot;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.models.Zone;
import fr.epsi.mspr2.domain.models.ZoneSummary;
import fr.epsi.mspr2.domain.ports.CapteurPort;
import fr.epsi.mspr2.domain.ports.EntrepotPort;
import fr.epsi.mspr2.domain.ports.MesurePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static fr.epsi.mspr2.domain.support.Fixtures.CLOCK;
import static fr.epsi.mspr2.domain.support.Fixtures.MAINTENANT;
import static fr.epsi.mspr2.domain.support.Fixtures.capteur;
import static fr.epsi.mspr2.domain.support.Fixtures.entrepot;
import static fr.epsi.mspr2.domain.support.Fixtures.mesure;
import static fr.epsi.mspr2.domain.support.Fixtures.zone;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntrepotServiceTest {

    @Mock
    EntrepotPort entrepotPort;
    @Mock
    CapteurPort capteurPort;
    @Mock
    MesurePort mesurePort;

    EntrepotService service;

    @BeforeEach
    void setUp() {
        service = new EntrepotService(entrepotPort, capteurPort, mesurePort, CLOCK);
    }

    @Test
    void getAllEntrepots_delegueAuPort() {
        List<Entrepot> entrepots = List.of(entrepot(), entrepot());
        when(entrepotPort.findAll()).thenReturn(entrepots);

        assertThat(service.getAllEntrepots()).isEqualTo(entrepots);
    }

    @Test
    void findZonesByEntrepotId_delegueAuPort() {
        UUID idEntrepot = UUID.randomUUID();
        List<ZoneSummary> zones = List.of(new ZoneSummary(UUID.randomUUID(), "A", idEntrepot, UUID.randomUUID(), 3L));
        when(entrepotPort.findZoneSummariesByEntrepotId(idEntrepot)).thenReturn(zones);

        assertThat(service.findZonesByEntrepotId(idEntrepot)).isEqualTo(zones);
    }

    @Test
    void findZonesByEntrepotId_exigeUnIdentifiant() {
        assertThatThrownBy(() -> service.findZonesByEntrepotId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID de zone ne peut pas être null");
        verifyNoInteractions(entrepotPort);
    }

    @Test
    void getCapteurSummaryByEntrepotId_exigeUnIdentifiant() {
        assertThatThrownBy(() -> service.getCapteurSummaryByEntrepotId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Entrepot ID ne peut pas être null");
        verifyNoInteractions(capteurPort, mesurePort);
    }

    @Test
    void getCapteurSummaryByEntrepotId_derniereMesureEtMoyennesSur24Heures() {
        UUID idEntrepot = UUID.randomUUID();
        Zone zone = zone("A");
        Capteur capteur = capteur(zone);
        LocalDateTime depuis24h = MAINTENANT.minusHours(24);
        Mesure derniere = mesure(capteur, "25.00", "45.00");
        Mesure plusAncienne = mesure(capteur, "20.00", "40.00");
        Mesure imprecise = mesure(capteur, "21.00", "41.01");
        when(capteurPort.findByEntrepotId(idEntrepot)).thenReturn(List.of(capteur));
        when(mesurePort.findLatestByCapteurId(capteur.getIdCapteur(), 1)).thenReturn(List.of(derniere));
        when(mesurePort.findByCapteurIdSince(capteur.getIdCapteur(), depuis24h))
                .thenReturn(List.of(derniere, plusAncienne, imprecise));

        List<CapteurSummary> resume = service.getCapteurSummaryByEntrepotId(idEntrepot);

        assertThat(resume).singleElement().satisfies(summary -> {
            assertThat(summary.zoneId()).isEqualTo(zone.getIdZone());
            assertThat(summary.zoneName()).isEqualTo("A");
            assertThat(summary.lastTemp()).isEqualByComparingTo("25.00");
            assertThat(summary.lastHumidity()).isEqualByComparingTo("45.00");
            assertThat(summary.avgTemp24h()).isEqualTo(new BigDecimal("22.00"));
            assertThat(summary.avgHumidity24h()).isEqualTo(new BigDecimal("42.00")); // (45 + 40 + 41.01) / 3 = 42.003...
        });
    }

    @Test
    void getCapteurSummaryByEntrepotId_laMoyenneEstArrondieAuCentieme() {
        UUID idEntrepot = UUID.randomUUID();
        Capteur capteur = capteur(zone("A"));
        when(capteurPort.findByEntrepotId(idEntrepot)).thenReturn(List.of(capteur));
        when(mesurePort.findLatestByCapteurId(capteur.getIdCapteur(), 1)).thenReturn(List.of());
        when(mesurePort.findByCapteurIdSince(capteur.getIdCapteur(), MAINTENANT.minusHours(24))).thenReturn(List.of(
                mesure(capteur, "10.00", "10.00"), mesure(capteur, "10.00", "10.00"), mesure(capteur, "10.01", "10.00")));

        assertThat(service.getCapteurSummaryByEntrepotId(idEntrepot)).singleElement()
                .satisfies(summary -> assertThat(summary.avgTemp24h()).isEqualTo(new BigDecimal("10.00")));
    }

    @Test
    void getCapteurSummaryByEntrepotId_sansMesure_toutEstNul() {
        UUID idEntrepot = UUID.randomUUID();
        Zone zone = zone("B");
        Capteur capteur = capteur(zone);
        when(capteurPort.findByEntrepotId(idEntrepot)).thenReturn(List.of(capteur));
        when(mesurePort.findLatestByCapteurId(capteur.getIdCapteur(), 1)).thenReturn(List.of());
        when(mesurePort.findByCapteurIdSince(capteur.getIdCapteur(), MAINTENANT.minusHours(24))).thenReturn(List.of());

        assertThat(service.getCapteurSummaryByEntrepotId(idEntrepot)).containsExactly(
                new CapteurSummary(zone.getIdZone(), "B", null, null, null, null));
    }

    @Test
    void getCapteurSummaryByEntrepotId_derniereMesureAncienne_pasDeMoyenneMaisLaDerniereValeurEstRenvoyee() {
        UUID idEntrepot = UUID.randomUUID();
        Capteur capteur = capteur(zone("C"));
        Mesure ancienne = mesure(capteur, "27.00", "50.00");
        when(capteurPort.findByEntrepotId(idEntrepot)).thenReturn(List.of(capteur));
        when(mesurePort.findLatestByCapteurId(capteur.getIdCapteur(), 1)).thenReturn(List.of(ancienne));
        when(mesurePort.findByCapteurIdSince(capteur.getIdCapteur(), MAINTENANT.minusHours(24))).thenReturn(List.of());

        assertThat(service.getCapteurSummaryByEntrepotId(idEntrepot)).singleElement().satisfies(summary -> {
            assertThat(summary.lastTemp()).isEqualByComparingTo("27.00");
            assertThat(summary.avgTemp24h()).isNull();
            assertThat(summary.avgHumidity24h()).isNull();
        });
    }

    @Test
    void getCapteurSummaryByEntrepotId_unResumeParCapteur() {
        UUID idEntrepot = UUID.randomUUID();
        Capteur premier = capteur(zone("A"));
        Capteur second = capteur(zone("B"));
        when(capteurPort.findByEntrepotId(idEntrepot)).thenReturn(List.of(premier, second));
        when(mesurePort.findLatestByCapteurId(premier.getIdCapteur(), 1)).thenReturn(List.of());
        when(mesurePort.findLatestByCapteurId(second.getIdCapteur(), 1)).thenReturn(List.of());
        when(mesurePort.findByCapteurIdSince(premier.getIdCapteur(), MAINTENANT.minusHours(24))).thenReturn(List.of());
        when(mesurePort.findByCapteurIdSince(second.getIdCapteur(), MAINTENANT.minusHours(24))).thenReturn(List.of());

        assertThat(service.getCapteurSummaryByEntrepotId(idEntrepot)).extracting(CapteurSummary::zoneName)
                .containsExactly("A", "B");
    }

    @Test
    void getCapteurSummaryByEntrepotId_unCapteurSansZone() {
        UUID idEntrepot = UUID.randomUUID();
        Capteur sansZone = capteur(null);
        when(capteurPort.findByEntrepotId(idEntrepot)).thenReturn(List.of(sansZone));
        when(mesurePort.findLatestByCapteurId(sansZone.getIdCapteur(), 1)).thenReturn(List.of());
        when(mesurePort.findByCapteurIdSince(sansZone.getIdCapteur(), MAINTENANT.minusHours(24))).thenReturn(List.of());

        assertThat(service.getCapteurSummaryByEntrepotId(idEntrepot)).containsExactly(
                new CapteurSummary(null, null, null, null, null, null));
    }
}
