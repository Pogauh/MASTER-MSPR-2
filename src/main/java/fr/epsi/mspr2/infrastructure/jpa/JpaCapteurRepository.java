package fr.epsi.mspr2.infrastructure.jpa;

import fr.epsi.mspr2.domain.models.Capteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaCapteurRepository extends JpaRepository<Capteur, UUID> {
	@Query("""
		select count(c) > 0
		from Capteur c
		join c.zone z
		join z.entrepot e
		where c.idCapteur = :idCapteur and e.idEntrepot = :idEntrepot
		""")
	boolean existsByIdCapteurAndEntrepotIdEntrepot(@Param("idCapteur") UUID idCapteur, @Param("idEntrepot") UUID idEntrepot);

	@Query("""
		select c from Capteur c
		join fetch c.zone z
		join fetch z.entrepot e
		where c.idCapteur = :capteurId
		""")
	Optional<Capteur> findByIdWithZoneAndEntrepot(@Param("capteurId") UUID capteurId);

	default Optional<Capteur> findByIdWithEntrepotAndPays(UUID capteurId) {
		return findByIdWithZoneAndEntrepot(capteurId);
	}

	Optional<Capteur> findFirstByZoneEntrepotIdEntrepotOrderByIdCapteurAsc(@Param("idEntrepot") UUID idEntrepot);
}