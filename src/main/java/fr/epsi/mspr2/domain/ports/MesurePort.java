package fr.epsi.mspr2.domain.ports;

import fr.epsi.mspr2.domain.models.Mesure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MesurePort {

    /** Persiste la mesure et renvoie son état persistant (identifiant généré compris). */
    Mesure save(Mesure mesure);

    Optional<Mesure> findById(UUID idMesure);

    void deleteById(UUID idMesure);

    List<Mesure> findByCapteurId(UUID idCapteur);

    /** Les {@code limit} dernières mesures du capteur, de la plus récente à la plus ancienne. */
    List<Mesure> findLatestByCapteurId(UUID idCapteur, int limit);

    /** Les mesures du capteur effectuées à partir de la date donnée (incluse). */
    List<Mesure> findByCapteurIdSince(UUID idCapteur, LocalDateTime since);

    /** Les mesures des capteurs de la zone dans la période (bornes incluses). */
    List<Mesure> findByZoneIdBetween(UUID idZone, LocalDateTime start, LocalDateTime end);

    Optional<Mesure> findLatestByZoneId(UUID idZone);
}
