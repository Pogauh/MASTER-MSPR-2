package fr.epsi.mspr2.infrastructure.jpa;

import fr.epsi.mspr2.domain.models.Capteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaCapteurRepository extends JpaRepository<Capteur, UUID> {
	boolean existsByIdCapteurAndEntrepotIdEntrepot(UUID idCapteur, UUID idEntrepot);

	Optional<Capteur> findByIdCapteurAndEntrepotIdEntrepot(UUID idCapteur, UUID idEntrepot);

	@Query("""
		select c from Capteur c
		join fetch c.entrepot e
		join fetch e.pays p
		where c.idCapteur = :capteurId
		""")
	Optional<Capteur> findByIdWithEntrepotAndPays(@Param("capteurId") UUID capteurId);

	Optional<Capteur> findFirstByEntrepotIdEntrepot(UUID idEntrepot);
}

