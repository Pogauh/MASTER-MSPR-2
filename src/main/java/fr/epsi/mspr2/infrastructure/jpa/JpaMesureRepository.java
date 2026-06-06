package fr.epsi.mspr2.infrastructure.jpa;

import fr.epsi.mspr2.domain.models.Mesure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaMesureRepository extends JpaRepository<Mesure, UUID> {

    @Query("""
        select m
        from Mesure m
        join m.capteur c
        join c.zone z
        where c.idCapteur = :capteurId
        """)
    List<Mesure> findAllByIdCapteur(@Param("capteurId") UUID capteurId);

    @Query("""
        SELECT m
        FROM Mesure m
        JOIN m.capteur c
        WHERE c.zone.idZone = :zoneId
          AND m.dateMesure BETWEEN :start AND :end
        """)
    Optional<List<Mesure>> findAllByZoneIdAndDateMesureBetween(
            @Param("zoneId") UUID zoneId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query(value = """
        SELECT m.*
        FROM mesure m
        JOIN capteur c ON c.id_capteur = m.id_capteur
        JOIN zone z ON z.id_zone = c.id_zone
        WHERE c.id_capteur = :capteurId
          AND CAST(m.date_mesure AS time) BETWEEN :startTime AND :endTime
        """, nativeQuery = true)
    List<Mesure> findAllByCapteurIdAndHourRange(
        @Param("capteurId") UUID capteurId,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );
}