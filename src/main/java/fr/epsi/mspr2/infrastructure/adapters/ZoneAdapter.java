package fr.epsi.mspr2.infrastructure.adapters;

import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.ZonePort;
import fr.epsi.mspr2.infrastructure.jpa.JpaMesureRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Component
public class ZoneAdapter implements ZonePort {

    private final JpaMesureRepository jpaMesureRepository;

        @Override
        public Optional<List<Mesure>> findMesuresByZoneId(UUID zoneId, LocalDateTime startDate, LocalDateTime endDate) {

            return jpaMesureRepository.findAllByZoneIdAndDateMesureBetween(
                    zoneId,
                    startDate,
                    endDate
            );
        }

    @Override
    public Optional<Mesure> findLastMesuresByZoneId(UUID zoneId) {

        return jpaMesureRepository.findFirstByCapteurZoneIdZoneOrderByDateMesureDesc(zoneId);
    }
}
