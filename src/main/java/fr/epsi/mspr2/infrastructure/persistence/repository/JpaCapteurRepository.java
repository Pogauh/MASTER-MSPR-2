package fr.epsi.mspr2.infrastructure.persistence.repository;

import fr.epsi.mspr2.infrastructure.persistence.entity.CapteurEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaCapteurRepository extends JpaRepository<CapteurEntity, UUID> {

    @Query("""
            select c from CapteurEntity c
            join fetch c.zone z
            join fetch z.entrepot
            where c.idCapteur = :capteurId
            """)
    Optional<CapteurEntity> findByCapteurId(@Param("capteurId") UUID capteurId);

    @Query("""
            select c from CapteurEntity c
            join fetch c.zone z
            join fetch z.entrepot e
            where e.idEntrepot = :entrepotId
            """)
    List<CapteurEntity> findAllByEntrepotId(@Param("entrepotId") UUID entrepotId);

    @Query("select c.idCapteur from CapteurEntity c")
    List<UUID> findAllIds();
}
