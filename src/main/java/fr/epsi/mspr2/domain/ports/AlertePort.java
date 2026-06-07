package fr.epsi.mspr2.domain.ports;

import fr.epsi.mspr2.domain.models.AlerteDetails;
import fr.epsi.mspr2.domain.models.Mesure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AlertePort {
    void sendAlerte(Mesure mesure);

    List<AlerteDetails> findAlerteByEntrepot(UUID idEntrepot, LocalDateTime startDate, LocalDateTime endDate);


    boolean isCompatible(Mesure mesure);
}
