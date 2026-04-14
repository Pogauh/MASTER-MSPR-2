package fr.epsi.mspr2.infrastructure.jpa;

import fr.epsi.mspr2.domain.models.Mesure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface JpaMesureRepository extends JpaRepository<Mesure, UUID> {
    @Query("""
        select m
        from Mesure m
        join m.capteur c
        join c.zone z
        where z.entrepot.idEntrepot = :entrepotId
        """)
    List<Mesure> findAllByCapteurEntrepotIdEntrepot(@Param("entrepotId") UUID entrepotId);

    @Query("""
        select m
        from Mesure m
        join m.capteur c
        join c.zone z
        where c.idCapteur = :capteurId
          and z.entrepot.idEntrepot = :entrepotId
        """)
    List<Mesure> findAllByCapteurIdCapteurAndCapteurEntrepotIdEntrepot(@Param("capteurId") UUID capteurId, @Param("entrepotId") UUID entrepotId);

    @Query("""
        select m
        from Mesure m
        join m.capteur c
        join c.zone z
        where z.entrepot.idEntrepot = :entrepotId
          and m.dateMesure between :start and :end
        """)
    List<Mesure> findAllByCapteurEntrepotIdEntrepotAndDateMesureBetween(@Param("entrepotId") UUID entrepotId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
        select m
        from Mesure m
        join m.capteur c
        join c.zone z
        where c.idCapteur = :capteurId
          and z.entrepot.idEntrepot = :entrepotId
          and m.dateMesure between :start and :end
        """)
    List<Mesure> findAllByCapteurIdCapteurAndCapteurEntrepotIdEntrepotAndDateMesureBetween(
        @Param("capteurId") UUID capteurId,
        @Param("entrepotId") UUID entrepotId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query(value = """
        SELECT m.*
        FROM mesure m
        JOIN capteur c ON c.id_capteur = m.id_capteur
        JOIN zone z ON z.id_zone = c.id_zone
        WHERE z.id_entrepot = :entrepotId
          AND CAST(m.date_mesure AS time) BETWEEN :startTime AND :endTime
        """, nativeQuery = true)
    List<Mesure> findAllByEntrepotIdAndHourRange(
        @Param("entrepotId") UUID entrepotId,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );

    @Query(value = """
        SELECT m.*
        FROM mesure m
        JOIN capteur c ON c.id_capteur = m.id_capteur
        JOIN zone z ON z.id_zone = c.id_zone
        WHERE c.id_capteur = :capteurId
          AND z.id_entrepot = :entrepotId
          AND CAST(m.date_mesure AS time) BETWEEN :startTime AND :endTime
        """, nativeQuery = true)
    List<Mesure> findAllByCapteurIdAndEntrepotIdAndHourRange(
        @Param("capteurId") UUID capteurId,
        @Param("entrepotId") UUID entrepotId,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );
}

