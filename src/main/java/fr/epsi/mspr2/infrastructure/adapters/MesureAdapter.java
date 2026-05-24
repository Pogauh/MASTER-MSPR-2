package fr.epsi.mspr2.infrastructure.adapters;

import fr.epsi.mspr2.infrastructure.jpa.JpaMesureRepository;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.MesurePort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@AllArgsConstructor
@Component
public class MesureAdapter implements MesurePort {

    private final JpaMesureRepository jpaMesureRepository;

    @Override
    public void save(Mesure mesure) {
        UUID capteurId = mesure.getCapteur() != null ? mesure.getCapteur().getIdCapteur() : null;
        if (capteurId == null) {
            throw new IllegalArgumentException("L'id du capteur est obligatoire pour enregistrer une mesure");
        }
        jpaMesureRepository.save(mesure);
    }

    @Override
    public Mesure findById(UUID idMesure) {
        return jpaMesureRepository.findById(idMesure).orElse(null);
    }

    @Override
    public void delete(UUID idMesure) {
        jpaMesureRepository.deleteById(idMesure);
    }
}