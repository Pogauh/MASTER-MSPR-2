package fr.epsi.mspr2.infrastructure.adapters;

import fr.epsi.mspr2.infrastructure.jpa.JpaMesureRepository;
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

    @Override
    public List<Mesure> findMesuresByCapteurId(UUID capteurId) {
        return jpaMesureRepository.findAllByIdCapteur(capteurId);
    }

    @Override
    public List<Mesure> findMesuresByCapteurIdAndDayRange(UUID capteurId, LocalDate startDate, LocalDate endDate) {
        return jpaMesureRepository.findAllByCapteurIdAndDateMesureBetween(
            capteurId,
            toStartOfDay(startDate),
            toEndOfDay(endDate)
        );
    }

    @Override
    public List<Mesure> findMesuresByCapteurIdAndHourRange(UUID capteurId, LocalTime startTime, LocalTime endTime) {
        return jpaMesureRepository.findAllByCapteurIdAndHourRange(capteurId, startTime, endTime);
    }


    private LocalDateTime toStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    private LocalDateTime toEndOfDay(LocalDate date) {
        return date.plusDays(1).atStartOfDay().minusNanos(1);
    }
}