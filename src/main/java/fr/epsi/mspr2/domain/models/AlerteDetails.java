package fr.epsi.mspr2.domain.models;

import java.time.LocalDateTime;
import java.util.UUID;

/** Alerte vue côté consultation : rattachée à la zone du capteur qui l'a déclenchée. */
public record AlerteDetails(UUID idAlerte, TypeAlerteEnum typeAlerte, String description, LocalDateTime dateEmission, UUID idZone) {
}
