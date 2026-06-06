package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.ZonePort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Service
public class ZoneService {

    private final ZonePort zonePort;

    public Optional<List<Mesure>> findMesuresByZoneIdAndDayRange(UUID zoneId, LocalDate startDate, LocalDate endDate) {
        if (zoneId == null) {
            throw new IllegalArgumentException("ZoneId est obligatoire");
        }
        validateDayRange(startDate, endDate);
        return zonePort.findMesuresByZoneIdAndDayRange(zoneId, startDate, endDate);
    }

    private void validateDayRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate et endDate sont obligatoires");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate doit être supérieur ou égal à startDate");
        }
    }

    private void validateHourRange(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("startTime et endTime sont obligatoires");
        }
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("endTime doit être supérieur ou égal à startTime");
        }
    }

}
