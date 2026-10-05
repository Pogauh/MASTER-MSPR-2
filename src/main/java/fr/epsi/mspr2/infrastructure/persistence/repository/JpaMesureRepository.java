package fr.epsi.mspr2.infrastructure.persistence.repository;

import fr.epsi.mspr2.infrastructure.persistence.entity.MesureEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaMesureRepository extends JpaRepository<MesureEntity, UUID> {

    List<MesureEntity> findByCapteurIdCapteur(UUID idCapteur);

    /** Mesures datées du capteur, de la plus récente à la plus ancienne ; la taille de la page borne le résultat. */
    List<MesureEntity> findByCapteurIdCapteurAndDateMesureIsNotNullOrderByDateMesureDesc(UUID idCapteur, Pageable page);

    List<MesureEntity> findByCapteurIdCapteurAndDateMesureGreaterThanEqual(UUID idCapteur, LocalDateTime since);

    @Query("""
            select m
            from MesureEntity m
            where m.capteur.zone.idZone = :zoneId
              and m.dateMesure between :start and :end
            """)
    List<MesureEntity> findAllByZoneIdAndDateMesureBetween(
            @Param("zoneId") UUID zoneId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    Optional<MesureEntity> findFirstByCapteurZoneIdZoneOrderByDateMesureDesc(UUID zoneId);
}
