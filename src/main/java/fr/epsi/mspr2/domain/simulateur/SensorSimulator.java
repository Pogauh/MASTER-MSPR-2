package fr.epsi.mspr2.domain.simulateur;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.epsi.mspr2.infrastructure.config.MqttGateway;
import fr.epsi.mspr2.infrastructure.config.EntrepotOwnerProperties;
import fr.epsi.mspr2.infrastructure.jpa.JpaCapteurRepository;
import fr.epsi.mspr2.domain.models.Capteur;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SensorSimulator {

    private final MqttGateway mqttGateway;
    private final JpaCapteurRepository jpaCapteurRepository;
    private final EntrepotOwnerProperties entrepotOwnerProperties;
    private final ObjectMapper objectMapper;

    // 900 000 ms = 15 minutes
    // Pour tests, mettre 10000 (10 secondes)
    @Scheduled(fixedRate = 10000)
    public void simulateSensorData() {
        UUID capteurId = resolveCapteurId();
        if (capteurId == null) {
            log.warn("Simulation ignorée: aucun capteur trouvé pour l'entrepôt {}", entrepotOwnerProperties.getEntrepotId());
            return;
        }

        Map<String, Object> payloadMap = new LinkedHashMap<>();
        payloadMap.put("temperature", randomInRange(26.0, 33.0));
        payloadMap.put("humidite", randomInRange(52.0, 58.0));
        payloadMap.put("dateMesure", LocalDateTime.now());
        payloadMap.put("idCapteur", capteurId);

        try {
            String payload = objectMapper.writeValueAsString(payloadMap);
            mqttGateway.sendToMqtt(payload, "futurekawa/bresil/entrepot1/data");
            log.info("Donnée simulée envoyée au broker: {}", payload);
        } catch (JsonProcessingException e) {
            log.error("Impossible de sérialiser la donnée simulée MQTT", e);
        }
    }

    private UUID resolveCapteurId() {
        return jpaCapteurRepository.findFirstByEntrepotIdEntrepot(entrepotOwnerProperties.getEntrepotId())
            .map(Capteur::getIdCapteur)
            .orElse(null);
    }

    private BigDecimal randomInRange(double min, double max) {
        return BigDecimal.valueOf(min + (Math.random() * (max - min))).setScale(2, RoundingMode.HALF_UP);
    }
}