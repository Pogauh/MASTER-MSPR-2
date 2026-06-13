package fr.epsi.mspr2.infrastructure.jpa;

import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface JpaHistoriqueRepository extends JpaRepository<Historique, UUID> {

    @Query("SELECT h FROM Historique h WHERE h.lot.idLot = :idLot")
    List<Historique> findAllHistoriqueForLot(UUID idLot);

    @Query("SELECT h FROM Historique h WHERE h.lot.idLot = :idLot AND h.type = :type")
    List<Historique> findAllHistoriqueForLotByType(UUID idLot, TypeHistoriqueEnum type);

    Integer countByDateEmissionAfterAndType(LocalDateTime date, TypeHistoriqueEnum type);
}
