package fr.epsi.mspr2.infrastructure.jpa;

import fr.epsi.mspr2.domain.models.Lot;
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
        select l
        from Lot l
        join l.zone z
        where z.entrepot.idEntrepot = :entrepotId
          and lower(z.nom) = lower(:emplacement)
        """)
    List<Lot> findAllByEntrepotIdEntrepotAndEmplacementIgnoreCase(@Param("entrepotId") UUID entrepotId, @Param("emplacement") String emplacement);

    @Query("""
        select distinct l
        from Lot l
        left join fetch l.historique h
        where l.idLot = :idLot
        """)
    Optional<Lot> findByIdWithHistorique(@Param("idLot") UUID idLot);
}