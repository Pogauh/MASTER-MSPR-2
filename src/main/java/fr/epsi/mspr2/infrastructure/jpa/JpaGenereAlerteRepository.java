package fr.epsi.mspr2.infrastructure.jpa;

import fr.epsi.mspr2.domain.models.GenereAlerte;
import fr.epsi.mspr2.domain.models.GenereAlerteId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaGenereAlerteRepository extends JpaRepository<GenereAlerte, GenereAlerteId> {
}
