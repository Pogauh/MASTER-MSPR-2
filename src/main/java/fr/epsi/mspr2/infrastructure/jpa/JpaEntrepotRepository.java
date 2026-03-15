package fr.epsi.mspr2.infrastructure.jpa;

import fr.epsi.mspr2.domain.models.Entrepot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface JpaEntrepotRepository extends JpaRepository<Entrepot, UUID> {
}

