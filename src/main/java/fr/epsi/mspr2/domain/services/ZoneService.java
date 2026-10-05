package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.MesurePort;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class ZoneService {

    private final MesurePort mesurePort;

    public List<Mesure> findMesuresByZoneId(UUID zoneId, LocalDateTime startDate, LocalDateTime endDate) {
        if (zoneId == null) {
            throw new IllegalArgumentException("ZoneId est obligatoire");
        }
        validateDateTimeRange(startDate, endDate);
        return mesurePort.findByZoneIdBetween(zoneId, startDate, endDate);
    }

    public Optional<Mesure> findLastMesuresByZoneId(UUID zoneId) {
        if (zoneId == null) {
            throw new IllegalArgumentException("ZoneId est obligatoire");
        }
        return mesurePort.findLatestByZoneId(zoneId);
    }

    private void validateDateTimeRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate et endDate sont obligatoires");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate doit être supérieur ou égal à startDate");
        }
    }
}
