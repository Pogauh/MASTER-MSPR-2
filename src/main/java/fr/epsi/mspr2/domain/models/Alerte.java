package fr.epsi.mspr2.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Alerte levée soit par une série de mesures hors tolérance (capteur renseigné),
 * soit par l'expiration d'un lot (pas de capteur).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alerte {
    private UUID idAlerte;
    private TypeAlerteEnum typeAlerte;
    private String description;
    private LocalDateTime dateEmission;
    private Capteur capteur;
}
