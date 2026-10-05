package fr.epsi.mspr2.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/** Emplacement de stockage d'un entrepôt. Une zone est surveillée par un capteur. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Zone {
    private UUID idZone;
    private String nom;
    private Entrepot entrepot;
}
