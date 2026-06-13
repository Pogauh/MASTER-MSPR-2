package fr.epsi.mspr2.infrastructure.jpa;

import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.StatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaLotRepository extends JpaRepository<Lot, UUID> {

    @Query("""
        select distinct l
        from Lot l
        left join fetch l.historique h
        where l.idLot = :idLot
        """)
    Optional<Lot> findByIdWithHistorique(@Param("idLot") UUID idLot);

    List<Lot> findByZoneIdZone(UUID idZone);

    Lot findFirstByStatusOrderByDateStockageAsc(StatusEnum status);

    Integer countByStatusIn(List<StatusEnum> statuses);

    @Query(value = "SELECT AVG(EXTRACT(DAY FROM (CURRENT_TIMESTAMP - l.date_stockage))) FROM lot l WHERE l.date_stockage IS NOT NULL", nativeQuery = true)
    Double findAverageAgeInDays();


}