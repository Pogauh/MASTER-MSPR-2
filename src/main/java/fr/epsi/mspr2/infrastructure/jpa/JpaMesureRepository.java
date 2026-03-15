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
    List<Mesure> findAllByCapteurEntrepotIdEntrepot(UUID entrepotId);

    List<Mesure> findAllByCapteurIdCapteurAndCapteurEntrepotIdEntrepot(UUID capteurId, UUID entrepotId);

    List<Mesure> findAllByCapteurEntrepotIdEntrepotAndDateMesureBetween(UUID entrepotId, LocalDateTime start, LocalDateTime end);

    List<Mesure> findAllByCapteurIdCapteurAndCapteurEntrepotIdEntrepotAndDateMesureBetween(
        UUID capteurId,
        UUID entrepotId,
        LocalDateTime start,
        LocalDateTime end
    );

    @Query(value = """
        SELECT m.*
        FROM mesure m
        JOIN capteur c ON c.id_capteur = m.id_capteur
        WHERE c.id_entrepot = :entrepotId
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
        WHERE c.id_capteur = :capteurId
          AND c.id_entrepot = :entrepotId
          AND CAST(m.date_mesure AS time) BETWEEN :startTime AND :endTime
        """, nativeQuery = true)
    List<Mesure> findAllByCapteurIdAndEntrepotIdAndHourRange(
        @Param("capteurId") UUID capteurId,
        @Param("entrepotId") UUID entrepotId,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );
}

