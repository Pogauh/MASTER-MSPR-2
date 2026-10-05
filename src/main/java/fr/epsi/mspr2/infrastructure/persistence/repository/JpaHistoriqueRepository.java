package fr.epsi.mspr2.infrastructure.persistence.repository;

import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.infrastructure.persistence.entity.HistoriqueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface JpaHistoriqueRepository extends JpaRepository<HistoriqueEntity, UUID> {

    @Query("select h from HistoriqueEntity h where h.lot.idLot = :idLot")
    List<HistoriqueEntity> findAllByLotId(@Param("idLot") UUID idLot);

    @Query("select h from HistoriqueEntity h where h.lot.idLot = :idLot and h.type = :type")
    List<HistoriqueEntity> findAllByLotIdAndType(@Param("idLot") UUID idLot, @Param("type") TypeHistoriqueEnum type);

    long countByDateEmissionAfterAndType(LocalDateTime date, TypeHistoriqueEnum type);
}
