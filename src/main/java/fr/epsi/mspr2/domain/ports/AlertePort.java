package fr.epsi.mspr2.domain.ports;

import fr.epsi.mspr2.domain.models.Alerte;
import fr.epsi.mspr2.domain.models.AlerteDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AlertePort {

    /** Persiste l'alerte et renvoie son état persistant (identifiant généré compris). */
    Alerte save(Alerte alerte);

    /**
     * Alertes déclenchées par les capteurs d'un entrepôt, éventuellement bornées dans le temps
     * (une borne {@code null} n'applique aucune limite).
     */
    List<AlerteDetails> findDetailsByEntrepotId(UUID idEntrepot, LocalDateTime start, LocalDateTime end);

    int countByDateEmissionBetween(LocalDateTime start, LocalDateTime end);
}
