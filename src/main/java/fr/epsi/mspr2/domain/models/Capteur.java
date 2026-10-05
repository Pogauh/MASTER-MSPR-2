package fr.epsi.mspr2.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Capteur {
    private UUID idCapteur;
    private String codeSerie;
    private Zone zone;

    /** L'entrepôt d'un capteur est celui de la zone qu'il surveille. */
    public Entrepot getEntrepot() {
        return zone != null ? zone.getEntrepot() : null;
    }
}
