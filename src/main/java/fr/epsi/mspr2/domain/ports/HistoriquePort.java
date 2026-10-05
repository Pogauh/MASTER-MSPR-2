package fr.epsi.mspr2.domain.ports;

import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface HistoriquePort {

    /** Persiste l'événement et renvoie son état persistant (identifiant généré compris). */
    Historique save(Historique historique);

    List<Historique> findByLotId(UUID idLot);

    List<Historique> findByLotIdAndType(UUID idLot, TypeHistoriqueEnum type);

    /** Nombre d'événements de ce type émis strictement après la date donnée. */
    long countByTypeAfter(TypeHistoriqueEnum type, LocalDateTime date);
}
