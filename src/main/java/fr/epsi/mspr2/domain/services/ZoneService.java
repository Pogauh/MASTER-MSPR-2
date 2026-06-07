package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.ZonePort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Service
public class ZoneService {

    private final ZonePort zonePort;

    public Optional<List<Mesure>> findMesuresByZoneIdAndDayRange(UUID zoneId, LocalDateTime startDate, LocalDateTime endDate) {
        if (zoneId == null) {
            throw new IllegalArgumentException("ZoneId est obligatoire");
        }
        validateDateTimeRange(startDate, endDate);
        return zonePort.findMesuresByZoneId(zoneId, startDate, endDate);
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
