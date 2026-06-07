package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.AlerteDetails;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.AlertePort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class AlerteService {

    private final AlertePort alertePort;

    public void sendAlerte(Mesure mesure) {
        if (alertePort.isCompatible(mesure)) {
            alertePort.sendAlerte(mesure);
        }
    }
    public List<AlerteDetails> getAlerteByEntrepot(UUID idEntrepot, LocalDateTime startDate, LocalDateTime endDate) {
        return alertePort.findAlerteByEntrepot(idEntrepot,startDate,endDate);
    }
}
