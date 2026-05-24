package fr.epsi.mspr2.domain.ports;

import fr.epsi.mspr2.domain.models.Mesure;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface CapteurPort {
    List<Mesure> findMesuresByCapteurId(UUID capteurId);

    List<Mesure> findMesuresByCapteurIdAndDayRange(UUID capteurId, LocalDate startDate, LocalDate endDate);

    List<Mesure> findMesuresByCapteurIdAndHourRange(UUID capteurId, LocalTime startTime, LocalTime endTime);
}