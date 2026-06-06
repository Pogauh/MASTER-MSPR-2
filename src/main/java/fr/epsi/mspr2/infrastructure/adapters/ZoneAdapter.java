package fr.epsi.mspr2.infrastructure.adapters;

import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.ZonePort;
import fr.epsi.mspr2.infrastructure.jpa.JpaMesureRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Component
public class ZoneAdapter implements ZonePort {

    private final JpaMesureRepository jpaMesureRepository;

        @Override
        public Optional<List<Mesure>> findMesuresByZoneIdAndDayRange(UUID zoneId, LocalDate startDate, LocalDate endDate) {

            return jpaMesureRepository.findAllByZoneIdAndDateMesureBetween(
                    zoneId,
                    toStartOfDay(startDate),
                    toEndOfDay(endDate)
            );
        }
    private LocalDateTime toStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    private LocalDateTime toEndOfDay(LocalDate date) {
        return date.plusDays(1).atStartOfDay().minusNanos(1);
    }
}
