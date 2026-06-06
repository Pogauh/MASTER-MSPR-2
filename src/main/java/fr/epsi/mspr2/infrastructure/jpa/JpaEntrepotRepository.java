package fr.epsi.mspr2.infrastructure.jpa;

import fr.epsi.mspr2.domain.models.Entrepot;
import fr.epsi.mspr2.domain.models.ZoneResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaEntrepotRepository extends JpaRepository<Entrepot, UUID> {

    @Query("SELECT new fr.epsi.mspr2.domain.models.ZoneResponse(" +
            "z.idZone, z.nom, e.idEntrepot, c.idCapteur, COUNT(l.idLot)) " +
            "FROM Zone z " +
            "JOIN z.entrepot e " +
            "LEFT JOIN Capteur c ON c.zone.idZone = z.idZone " +
            "LEFT JOIN Lot l ON l.zone.idZone = z.idZone " +
            "WHERE e.idEntrepot = :idEntrepot " +
            "GROUP BY z.idZone, z.nom, e.idEntrepot, c.idCapteur")
    List<ZoneResponse> findZoneByIdEntrepot(@Param("idEntrepot") UUID idEntrepot);
}
