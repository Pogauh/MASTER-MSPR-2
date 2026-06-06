package fr.epsi.mspr2.infrastructure.jpa;

import fr.epsi.mspr2.domain.models.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface JpaZoneRepository extends JpaRepository<Zone, UUID> {

}
