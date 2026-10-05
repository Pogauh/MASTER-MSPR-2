package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.MesurePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ZoneServiceTest {

    private final LocalDateTime debut = LocalDateTime.of(2026, 3, 1, 0, 0);
    private final LocalDateTime fin = LocalDateTime.of(2026, 3, 5, 0, 0);

    @Mock
    MesurePort mesurePort;
    @InjectMocks
    ZoneService service;

    @Test
    void findMesuresByZoneId_accepteUnePeriodeReduiteAUnInstant() {
        UUID idZone = UUID.randomUUID();
        List<Mesure> mesures = List.of(new Mesure());
        when(mesurePort.findByZoneIdBetween(idZone, debut, fin)).thenReturn(mesures);
        when(mesurePort.findByZoneIdBetween(idZone, debut, debut)).thenReturn(List.of());

        assertThat(service.findMesuresByZoneId(idZone, debut, fin)).isEqualTo(mesures);
        assertThat(service.findMesuresByZoneId(idZone, debut, debut)).isEmpty();
    }

    @Test
    void findMesuresByZoneId_exigeLaZoneEtLesDeuxDates() {
        UUID idZone = UUID.randomUUID();

        assertThatThrownBy(() -> service.findMesuresByZoneId(null, debut, fin))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("ZoneId est obligatoire");
        assertThatThrownBy(() -> service.findMesuresByZoneId(idZone, null, fin))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("startDate et endDate sont obligatoires");
        assertThatThrownBy(() -> service.findMesuresByZoneId(idZone, debut, null))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("startDate et endDate sont obligatoires");
        verifyNoInteractions(mesurePort);
    }

    @Test
    void findMesuresByZoneId_refuseUnePeriodeInversee() {
        assertThatThrownBy(() -> service.findMesuresByZoneId(UUID.randomUUID(), fin, debut))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("endDate doit être supérieur ou égal à startDate");
        verifyNoInteractions(mesurePort);
    }

    @Test
    void findLastMesuresByZoneId() {
        UUID idZone = UUID.randomUUID();
        Mesure mesure = new Mesure();
        when(mesurePort.findLatestByZoneId(idZone)).thenReturn(Optional.of(mesure));

        assertThat(service.findLastMesuresByZoneId(idZone)).contains(mesure);
    }

    @Test
    void findLastMesuresByZoneId_exigeLaZone() {
        assertThatThrownBy(() -> service.findLastMesuresByZoneId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ZoneId est obligatoire");
        verifyNoInteractions(mesurePort);
    }
}
