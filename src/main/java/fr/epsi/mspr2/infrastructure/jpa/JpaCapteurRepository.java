package fr.epsi.mspr2.infrastructure.jpa;

import fr.epsi.mspr2.domain.models.Capteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaCapteurRepository extends JpaRepository<Capteur, UUID> {
	@Query("""
		select c from Capteur c
		join fetch c.zone z
		where c.idCapteur = :capteurId
		""")
	Optional<Capteur> findByCapteurId(@Param("capteurId") UUID capteurId);

	@Query("""
		select c from Capteur c
		join fetch c.zone z
		join fetch z.entrepot e
		where e.idEntrepot = :entrepotId
		""")
	List<Capteur> findAllByEntrepotId(@Param("entrepotId") UUID entrepotId);
}