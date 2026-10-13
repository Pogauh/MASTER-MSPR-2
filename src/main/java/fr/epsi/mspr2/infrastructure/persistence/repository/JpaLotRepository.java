package fr.epsi.mspr2.infrastructure.persistence.repository;

import fr.epsi.mspr2.domain.models.StatusEnum;
import fr.epsi.mspr2.infrastructure.persistence.entity.LotEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaLotRepository extends JpaRepository<LotEntity, UUID> {

    /** Le lot avec son historique ; la zone et l'exploitation sont chargées avec lui (relations EAGER). */
    @Query("""
            select distinct l
            from LotEntity l
            left join fetch l.historique h
            where l.idLot = :idLot
            """)
    Optional<LotEntity> findByIdWithHistorique(@Param("idLot") UUID idLot);

    @Override
    @EntityGraph(attributePaths = {"zone", "zone.entrepot", "exploitation"})
    List<LotEntity> findAll();

    @EntityGraph(attributePaths = {"zone", "zone.entrepot", "exploitation"})
    List<LotEntity> findByZoneIdZone(UUID idZone);

    @EntityGraph(attributePaths = {"zone", "zone.entrepot", "exploitation"})
    List<LotEntity> findByDateStockageBefore(LocalDateTime date);

    @EntityGraph(attributePaths = {"zone", "zone.entrepot", "exploitation"})
    Optional<LotEntity> findFirstByStatusOrderByDateStockageAsc(StatusEnum status);

    long countByStatusIn(Collection<StatusEnum> statuses);

    @Query(value = "SELECT AVG(EXTRACT(DAY FROM (CURRENT_TIMESTAMP - l.date_stockage))) FROM lot l WHERE l.date_stockage IS NOT NULL",
            nativeQuery = true)
    Double findAverageAgeInDays();

    @Query("select l.idLot from LotEntity l where l.dateStockage between :startDate and :endDate")
    List<UUID> findIdsByDateStockageBetween(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);
}
