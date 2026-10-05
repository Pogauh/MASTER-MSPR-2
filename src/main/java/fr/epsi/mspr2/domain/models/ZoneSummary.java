package fr.epsi.mspr2.domain.models;

import java.util.UUID;

/** Zone d'un entrepôt avec son capteur et le nombre de lots qu'elle contient. */
public record ZoneSummary(UUID idZone, String nomZone, UUID idEntrepot, UUID idCapteur, Long lotCount) {
}
