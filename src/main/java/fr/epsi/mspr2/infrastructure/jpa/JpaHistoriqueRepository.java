package fr.epsi.mspr2.infrastructure.jpa;

import fr.epsi.mspr2.domain.models.Historique;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaHistoriqueRepository extends JpaRepository<Historique, UUID> {
}
