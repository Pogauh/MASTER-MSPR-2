package fr.epsi.mspr2.infrastructure.messaging.mqtt;

import fr.epsi.mspr2.domain.models.NouvelleMesure;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Charge utile JSON publiée par les capteurs (ESP32) sur le topic MQTT :
 * {@code {"temperature":27.50,"humidite":56.20,"dateMesure":"2026-03-15T16:00:00","idCapteur":"<uuid>"}}.
 */
public record MesureMessage(BigDecimal temperature, BigDecimal humidite, LocalDateTime dateMesure, UUID idCapteur) {

    public NouvelleMesure toNouvelleMesure() {
        return new NouvelleMesure(temperature, humidite, dateMesure, idCapteur);
    }
}
