package fr.epsi.mspr2.infrastructure.persistence.repository;

import java.util.UUID;

/** Ligne du résultat de {@link JpaEntrepotRepository#findZoneRowsByEntrepotId(UUID)}. */
public record ZoneRow(UUID idZone, String nom, UUID idEntrepot, UUID idCapteur, Long lotCount) {
}
