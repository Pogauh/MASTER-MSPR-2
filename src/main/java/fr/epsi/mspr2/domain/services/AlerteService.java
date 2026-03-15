package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.AlertePort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AlerteService {

    private final AlertePort alertePort;

    public void sendAlerte(Mesure mesure) {
        if (alertePort.isCompatible(mesure)) {
            alertePort.sendAlerte(mesure);
        }
    }
}
