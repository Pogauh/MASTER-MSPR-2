package fr.epsi.mspr2.infrastructure.persistence.repository;

import fr.epsi.mspr2.infrastructure.persistence.entity.AlerteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface JpaAlerteRepository extends JpaRepository<AlerteEntity, UUID> {

    /** Alertes déclenchées par un capteur de l'entrepôt ; une borne {@code null} n'applique aucune limite. */
    @Query("""
            select a
            from AlerteEntity a
            join fetch a.capteur c
            join fetch c.zone z
            where z.entrepot.idEntrepot = :idEntrepot
              and (a.dateEmission >= coalesce(:start, a.dateEmission))
              and (a.dateEmission <= coalesce(:end, a.dateEmission))
            """)
    List<AlerteEntity> findByEntrepotId(
            @Param("idEntrepot") UUID idEntrepot,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    int countByDateEmissionBetween(LocalDateTime startDate, LocalDateTime endDate);
}
