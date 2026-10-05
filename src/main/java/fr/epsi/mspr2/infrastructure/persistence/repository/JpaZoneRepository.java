package fr.epsi.mspr2.infrastructure.persistence.repository;

import fr.epsi.mspr2.infrastructure.persistence.entity.ZoneEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaZoneRepository extends JpaRepository<ZoneEntity, UUID> {
}
