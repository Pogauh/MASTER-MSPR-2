package fr.epsi.mspr2.infrastructure.persistence.repository;

import fr.epsi.mspr2.infrastructure.persistence.entity.EntrepotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaEntrepotRepository extends JpaRepository<EntrepotEntity, UUID> {

    @Query("""
            select new fr.epsi.mspr2.infrastructure.persistence.repository.ZoneRow(
                z.idZone, z.nom, e.idEntrepot, c.idCapteur, count(l.idLot))
            from ZoneEntity z
            join z.entrepot e
            left join CapteurEntity c on c.zone.idZone = z.idZone
            left join LotEntity l on l.zone.idZone = z.idZone
            where e.idEntrepot = :idEntrepot
            group by z.idZone, z.nom, e.idEntrepot, c.idCapteur
            """)
    List<ZoneRow> findZoneRowsByEntrepotId(@Param("idEntrepot") UUID idEntrepot);
}
