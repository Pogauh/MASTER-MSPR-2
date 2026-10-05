package fr.epsi.mspr2.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Données d'une mesure à enregistrer, telles que reçues d'un capteur (REST ou MQTT).
 *
 * @param dateMesure date du relevé ; la date courante est utilisée si elle est absente
 */
public record NouvelleMesure(BigDecimal temperature, BigDecimal humidite, LocalDateTime dateMesure, UUID idCapteur) {
}
