package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.*;
import fr.epsi.mspr2.domain.ports.CapteurPort;
import fr.epsi.mspr2.domain.ports.EntrepotPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class EntrepotServiceTest {

    private EntrepotPort entrepotPort;
    private CapteurPort capteurPort;
    private EntrepotService service;

    @BeforeEach
    void setUp() {
        entrepotPort = mock(EntrepotPort.class);
        capteurPort = mock(CapteurPort.class);
        service = new EntrepotService(entrepotPort, capteurPort);
    }

    @Test
    void getAllEntrepots_shouldReturnList() {
        Entrepot e = new Entrepot();
        when(entrepotPort.findAll()).thenReturn(List.of(e));

        List<Entrepot> result = service.getAllEntrepots();

        assertThat(result).hasSize(1);
    }

    @Test
    void getCapteurSummaryByEntrepotId_shouldThrowWhenIdIsNull() {
        assertThatThrownBy(() -> service.getCapteurSummaryByEntrepotId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
    }

    @Test
    void getCapteurSummaryByEntrepotId_shouldReturnSummaryWithAverages() {
        UUID entrepotId = UUID.randomUUID();
        UUID capteurId = UUID.randomUUID();
        UUID zoneId = UUID.randomUUID();

        Zone zone = new Zone();
        zone.setIdZone(zoneId);
        zone.setNom("Zone A");

        Capteur capteur = new Capteur();
        capteur.setIdCapteur(capteurId);
        capteur.setZone(zone);

        Mesure m1 = new Mesure();
        m1.setTemperature(new BigDecimal("20.0"));
        m1.setHumidite(new BigDecimal("50.0"));
        m1.setDateMesure(LocalDateTime.now().minusHours(1));

        Mesure m2 = new Mesure();
        m2.setTemperature(new BigDecimal("22.0"));
        m2.setHumidite(new BigDecimal("60.0"));
        m2.setDateMesure(LocalDateTime.now().minusHours(2));

        when(capteurPort.findByEntrepotId(entrepotId)).thenReturn(List.of(capteur));
        when(capteurPort.findMesuresByCapteurId(capteurId)).thenReturn(List.of(m1, m2));

        List<CapteurSummary> result = service.getCapteurSummaryByEntrepotId(entrepotId);

        assertThat(result).hasSize(1);
        CapteurSummary summary = result.get(0);
        assertThat(summary.getZoneId()).isEqualTo(zoneId);
        assertThat(summary.getZoneName()).isEqualTo("Zone A");
        assertThat(summary.getAvgTemp24h()).isNotNull();
        assertThat(summary.getAvgHumidity24h()).isNotNull();
    }

    @Test
    void getCapteurSummaryByEntrepotId_shouldHandleNoCapteurs() {
        UUID entrepotId = UUID.randomUUID();
        when(capteurPort.findByEntrepotId(entrepotId)).thenReturn(List.of());

        List<CapteurSummary> result = service.getCapteurSummaryByEntrepotId(entrepotId);

        assertThat(result).isEmpty();
    }

    @Test
    void getCapteurSummaryByEntrepotId_shouldHandleCapteurWithNoZone() {
        UUID entrepotId = UUID.randomUUID();
        UUID capteurId = UUID.randomUUID();

        Capteur capteur = new Capteur();
        capteur.setIdCapteur(capteurId);
        capteur.setZone(null);

        when(capteurPort.findByEntrepotId(entrepotId)).thenReturn(List.of(capteur));
        when(capteurPort.findMesuresByCapteurId(capteurId)).thenReturn(List.of());

        List<CapteurSummary> result = service.getCapteurSummaryByEntrepotId(entrepotId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getZoneId()).isNull();
        assertThat(result.get(0).getLastTemp()).isNull();
    }

    @Test
    void findZonesByEntrepotId_shouldThrowWhenIdIsNull() {
        assertThatThrownBy(() -> service.findZonesByEntrepotId(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findZonesByEntrepotId_shouldReturnListFromPort() {
        UUID id = UUID.randomUUID();
        ZoneResponse zoneResponse = mock(ZoneResponse.class);
        when(entrepotPort.findZoneByIdEntrepot(id)).thenReturn(List.of(zoneResponse));

        List<ZoneResponse> result = service.findZonesByEntrepotId(id);

        assertThat(result).hasSize(1);
    }
}

