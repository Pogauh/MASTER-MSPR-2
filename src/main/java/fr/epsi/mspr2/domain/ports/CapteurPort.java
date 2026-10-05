package fr.epsi.mspr2.domain.ports;

import fr.epsi.mspr2.domain.models.Capteur;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CapteurPort {

    /** Le capteur est renvoyé avec sa zone et l'entrepôt de celle-ci. */
    Optional<Capteur> findById(UUID idCapteur);

    /** Les capteurs de l'entrepôt, avec leur zone. */
    List<Capteur> findByEntrepotId(UUID idEntrepot);

    List<UUID> findAllIds();
}
