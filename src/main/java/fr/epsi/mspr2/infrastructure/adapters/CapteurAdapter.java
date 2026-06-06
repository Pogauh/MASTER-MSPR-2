package fr.epsi.mspr2.infrastructure.adapters;

import fr.epsi.mspr2.infrastructure.jpa.JpaMesureRepository;
import fr.epsi.mspr2.infrastructure.jpa.JpaCapteurRepository;
import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.CapteurPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Component
public class CapteurAdapter implements CapteurPort {

    private final JpaMesureRepository jpaMesureRepository;
    private final JpaCapteurRepository jpaCapteurRepository;

    @Override
    public List<Capteur> findByEntrepotId(UUID entrepotId) {
        return jpaCapteurRepository.findAllByEntrepotId(entrepotId);
    }

    @Override
    public List<Mesure> findMesuresByCapteurId(UUID capteurId) {
        return jpaMesureRepository.findAllByIdCapteur(capteurId);
    }
}