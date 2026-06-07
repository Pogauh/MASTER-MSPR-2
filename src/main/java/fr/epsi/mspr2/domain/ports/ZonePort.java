package fr.epsi.mspr2.domain.ports;

import fr.epsi.mspr2.domain.models.Mesure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ZonePort {
    Optional<List<Mesure>> findMesuresByZoneId(UUID zoneId, LocalDateTime startDate, LocalDateTime endDate);

    Optional<Mesure> findLastMesuresByZoneId(UUID zoneId);
}
