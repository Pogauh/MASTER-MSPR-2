package fr.epsi.mspr2.infrastructure.adapters;

import fr.epsi.mspr2.infrastructure.jpa.JpaCapteurRepository;
import fr.epsi.mspr2.infrastructure.jpa.JpaMesureRepository;
import fr.epsi.mspr2.infrastructure.config.EntrepotOwnerProperties;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.CapteurPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Component
public class CapteurAdapter implements CapteurPort {

    private final JpaMesureRepository jpaMesureRepository;
    private final JpaCapteurRepository jpaCapteurRepository;
    private final EntrepotOwnerProperties entrepotOwnerProperties;

    @Override
    public List<Mesure> findAllMesuresByDayRange(LocalDate startDate, LocalDate endDate) {
        UUID entrepotId = entrepotOwnerProperties.getEntrepotId();
        return jpaMesureRepository.findAllByCapteurEntrepotIdEntrepotAndDateMesureBetween(
            entrepotId,
            toStartOfDay(startDate),
            toEndOfDay(endDate)
        );
    }

    @Override
    public List<Mesure> findAllMesuresByHourRange(LocalTime startTime, LocalTime endTime) {
        UUID entrepotId = entrepotOwnerProperties.getEntrepotId();
        return jpaMesureRepository.findAllByEntrepotIdAndHourRange(entrepotId, startTime, endTime);
    }

    @Override
    public List<Mesure> findMesuresByCapteurId(UUID capteurId) {
        UUID entrepotId = entrepotOwnerProperties.getEntrepotId();
        ensureCapteurBelongsToOwner(capteurId, entrepotId);
        return jpaMesureRepository.findAllByCapteurIdCapteurAndCapteurEntrepotIdEntrepot(capteurId, entrepotId);
    }

    @Override
    public List<Mesure> findMesuresByCapteurIdAndDayRange(UUID capteurId, LocalDate startDate, LocalDate endDate) {
        UUID entrepotId = entrepotOwnerProperties.getEntrepotId();
        ensureCapteurBelongsToOwner(capteurId, entrepotId);
        return jpaMesureRepository.findAllByCapteurIdCapteurAndCapteurEntrepotIdEntrepotAndDateMesureBetween(
            capteurId,
            entrepotId,
            toStartOfDay(startDate),
            toEndOfDay(endDate)
        );
    }

    @Override
    public List<Mesure> findMesuresByCapteurIdAndHourRange(UUID capteurId, LocalTime startTime, LocalTime endTime) {
        UUID entrepotId = entrepotOwnerProperties.getEntrepotId();
        ensureCapteurBelongsToOwner(capteurId, entrepotId);
        return jpaMesureRepository.findAllByCapteurIdAndEntrepotIdAndHourRange(capteurId, entrepotId, startTime, endTime);
    }

    private void ensureCapteurBelongsToOwner(UUID capteurId, UUID entrepotId) {
        boolean exists = jpaCapteurRepository.existsByIdCapteurAndEntrepotIdEntrepot(capteurId, entrepotId);
        if (!exists) {
            throw new IllegalStateException("Capteur introuvable pour l'id " + capteurId + " dans l'entrepôt propriétaire " + entrepotId);
        }
    }

    private LocalDateTime toStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    private LocalDateTime toEndOfDay(LocalDate date) {
        return date.plusDays(1).atStartOfDay().minusNanos(1);
    }
}

