package fr.epsi.mspr2.infrastructure.jpa;

import fr.epsi.mspr2.domain.models.Alerte;
import fr.epsi.mspr2.domain.models.AlerteDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface JpaAlerteRepository extends JpaRepository<Alerte, UUID> {
    @Query("""
        SELECT new fr.epsi.mspr2.domain.models.AlerteDetails(
            a.idAlerte,
            a.typeAlerte,
            a.description,
            a.dateEmission,
            z.idZone
        )
        FROM Alerte a
        JOIN a.capteur c
        JOIN c.zone z
        WHERE z.entrepot.idEntrepot = :idEntrepot
          AND (a.dateEmission >= COALESCE(:start, a.dateEmission))
          AND (a.dateEmission <= COALESCE(:end, a.dateEmission))
        """)
    List<AlerteDetails> findByEntrepotId(
            @Param("idEntrepot") UUID idEntrepot,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    int countByDateEmissionBetween(LocalDateTime startDate, LocalDateTime endDate);
}

