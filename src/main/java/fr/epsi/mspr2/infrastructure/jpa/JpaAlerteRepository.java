package fr.epsi.mspr2.infrastructure.jpa;

import fr.epsi.mspr2.domain.models.Alerte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaAlerteRepository extends JpaRepository<Alerte, UUID> {
}

