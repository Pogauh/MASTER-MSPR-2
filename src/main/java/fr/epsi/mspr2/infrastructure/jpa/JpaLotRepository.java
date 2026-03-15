package fr.epsi.mspr2.infrastructure.jpa;

import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.Entrepot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface JpaLotRepository extends JpaRepository<Lot, UUID> {
    List<Lot> findAllByEntrepot(Entrepot entrepot);

    List<Lot> findAllByEntrepotIdEntrepotAndEmplacementIgnoreCase(UUID entrepotId, String emplacement);

    @Query("""
        select distinct l
        from Lot l
        left join fetch l.exploitation e
        left join fetch l.entrepot en
        join fetch l.genereAlertes ga
        join fetch ga.alerte a
        where l.entrepot.idEntrepot = :entrepotId
          and l.dateSortie is null
        """)
    List<Lot> findAllWithAlerteAndDateSortieNullByEntrepotId(@Param("entrepotId") UUID entrepotId);
}