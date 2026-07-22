package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.AlerteDetails;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.AlertePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AlerteServiceTest {

    private AlertePort alertePort;
    private AlerteService service;

    @BeforeEach
    void setUp() {
        alertePort = mock(AlertePort.class);
        service = new AlerteService(alertePort);
    }

    @Test
    void sendAlerte_shouldCallSendWhenCompatible() {
        Mesure mesure = new Mesure();
        when(alertePort.isCompatible(mesure)).thenReturn(true);

        service.sendAlerte(mesure);

        verify(alertePort).sendAlerte(mesure);
    }

    @Test
    void sendAlerte_shouldNotCallSendWhenNotCompatible() {
        Mesure mesure = new Mesure();
        when(alertePort.isCompatible(mesure)).thenReturn(false);

        service.sendAlerte(mesure);

        verify(alertePort, never()).sendAlerte(any());
    }

    @Test
    void getAlerteByEntrepot_shouldReturnListFromPort() {
        UUID entrepotId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        AlerteDetails detail = mock(AlerteDetails.class);

        when(alertePort.findAlerteByEntrepot(entrepotId, start, end)).thenReturn(List.of(detail));

        List<AlerteDetails> result = service.getAlerteByEntrepot(entrepotId, start, end);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(detail);
    }

    @Test
    void getAlerteByEntrepot_shouldReturnEmptyListWhenNone() {
        UUID entrepotId = UUID.randomUUID();
        when(alertePort.findAlerteByEntrepot(any(), any(), any())).thenReturn(List.of());

        List<AlerteDetails> result = service.getAlerteByEntrepot(entrepotId, null, null);

        assertThat(result).isEmpty();
    }
}

