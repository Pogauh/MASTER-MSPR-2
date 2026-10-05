package fr.epsi.mspr2.domain.ports;

import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.StatusEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.OptionalDouble;

public interface LotPort {

    /** Crée ou met à jour le lot et renvoie son état persistant (identifiant généré compris). */
    Lot save(Lot lot);

    /** Le lot est renvoyé avec son historique. */
    Optional<Lot> findById(UUID idLot);

    void deleteById(UUID idLot);

    /** Les lots sont renvoyés avec leur historique. */
    List<Lot> findAll();

    /** Les lots de la zone, sans leur historique. */
    List<Lot> findByZoneId(UUID idZone);

    /** Les lots produits avant la date donnée, sans leur historique. */
    List<Lot> findByDateProductionBefore(LocalDate date);

    /** Le lot le plus anciennement stocké ayant ce statut. */
    Optional<Lot> findOldestByStatus(StatusEnum status);

    long count();

    long countByStatusIn(Collection<StatusEnum> statuses);

    /** Âge moyen, en jours entiers, des lots depuis leur date de stockage ; vide s'il n'y a aucun lot. */
    OptionalDouble findAverageAgeInDays();

    List<UUID> findIdsByDateStockageBetween(LocalDateTime start, LocalDateTime end);
}
