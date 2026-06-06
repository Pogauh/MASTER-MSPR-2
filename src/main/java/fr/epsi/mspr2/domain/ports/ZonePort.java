package fr.epsi.mspr2.domain.ports;

import fr.epsi.mspr2.domain.models.Mesure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ZonePort {
    Optional<List<Mesure>> findMesuresByZoneIdAndDayRange(UUID zoneId, LocalDate startDate, LocalDate endDate);
}
