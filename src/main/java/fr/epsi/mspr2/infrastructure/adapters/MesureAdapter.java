package fr.epsi.mspr2.infrastructure.adapters;

import fr.epsi.mspr2.infrastructure.jpa.JpaEntrepotRepository;
import fr.epsi.mspr2.infrastructure.jpa.JpaCapteurRepository;
import fr.epsi.mspr2.infrastructure.jpa.JpaMesureRepository;
import fr.epsi.mspr2.infrastructure.config.EntrepotOwnerProperties;
import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.MesurePort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Component
public class MesureAdapter implements MesurePort {

    private final JpaMesureRepository jpaMesureRepository;
    private final JpaCapteurRepository jpaCapteurRepository;
    private final JpaEntrepotRepository jpaEntrepotRepository;
    private final EntrepotOwnerProperties entrepotOwnerProperties;

    @Override
    public void save(Mesure mesure) {
        UUID capteurId = mesure.getCapteur() != null ? mesure.getCapteur().getIdCapteur() : null;
        if (capteurId == null) {
            throw new IllegalArgumentException("L'id du capteur est obligatoire pour enregistrer une mesure");
        }

        UUID entrepotOwnerId = entrepotOwnerProperties.getEntrepotId();
        Capteur capteur = jpaCapteurRepository.findById(capteurId)
            .orElseThrow(() -> new IllegalStateException("Capteur introuvable pour l'id " + capteurId));

        UUID capteurEntrepotId = capteur.getEntrepot() != null ? capteur.getEntrepot().getIdEntrepot() : null;
        if (!entrepotOwnerId.equals(capteurEntrepotId)) {
            throw new IllegalStateException("Le capteur " + capteurId + " n'appartient pas à l'entrepôt propriétaire " + entrepotOwnerId);
        }

        mesure.setCapteur(capteur);
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

    @Override
    public List<Mesure> findAllByEntrepot() {
        UUID entrepotId = entrepotOwnerProperties.getEntrepotId();
        jpaEntrepotRepository.findById(entrepotId)
            .orElseThrow(() -> new IllegalStateException("Entrepot propriétaire introuvable pour l'id " + entrepotId));
        return jpaMesureRepository.findAllByCapteurEntrepotIdEntrepot(entrepotId);
    }
}
