package fr.epsi.mspr2.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Relevé de température / humidité effectué par un capteur. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mesure {
    private UUID idMesure;
    private BigDecimal temperature;
    private BigDecimal humidite;
    private LocalDateTime dateMesure;
    private Capteur capteur;
}
