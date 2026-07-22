package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.ZonePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ZoneServiceTest {

    private ZonePort zonePort;
    private ZoneService service;

    @BeforeEach
    void setUp() {
        zonePort = mock(ZonePort.class);
        service = new ZoneService(zonePort);
    }

    @Test
    void findMesuresByZoneId_shouldThrowWhenZoneIdIsNull() {
        assertThatThrownBy(() -> service.findMesuresByZoneId(null, LocalDateTime.now(), LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ZoneId est obligatoire");
    }

    @Test
    void findMesuresByZoneId_shouldThrowWhenDatesAreNull() {
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> service.findMesuresByZoneId(id, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obligatoires");
    }

    @Test
    void findMesuresByZoneId_shouldThrowWhenEndBeforeStart() {
        UUID id = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.minusDays(1);

        assertThatThrownBy(() -> service.findMesuresByZoneId(id, start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("endDate");
    }

    @Test
    void findMesuresByZoneId_shouldReturnMesuresWhenValid() {
        UUID id = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        Mesure m = new Mesure();
        when(zonePort.findMesuresByZoneId(id, start, end)).thenReturn(Optional.of(List.of(m)));

        Optional<List<Mesure>> result = service.findMesuresByZoneId(id, start, end);

        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(1);
    }

    @Test
    void findLastMesuresByZoneId_shouldThrowWhenZoneIdIsNull() {
        assertThatThrownBy(() -> service.findLastMesuresByZoneId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ZoneId est obligatoire");
    }

    @Test
    void findLastMesuresByZoneId_shouldReturnMesureWhenFound() {
        UUID id = UUID.randomUUID();
        Mesure m = new Mesure();
        when(zonePort.findLastMesuresByZoneId(id)).thenReturn(Optional.of(m));

        Optional<Mesure> result = service.findLastMesuresByZoneId(id);

        assertThat(result).isPresent();
    }

    @Test
    void findLastMesuresByZoneId_shouldReturnEmptyWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(zonePort.findLastMesuresByZoneId(id)).thenReturn(Optional.empty());

        Optional<Mesure> result = service.findLastMesuresByZoneId(id);

        assertThat(result).isEmpty();
    }
}

