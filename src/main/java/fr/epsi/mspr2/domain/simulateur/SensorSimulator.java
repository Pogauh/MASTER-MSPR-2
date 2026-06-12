package fr.epsi.mspr2.domain.simulateur;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.epsi.mspr2.infrastructure.config.MqttGateway;
import fr.epsi.mspr2.infrastructure.jpa.JpaCapteurRepository;
import fr.epsi.mspr2.infrastructure.jpa.JpaEntrepotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    private UUID capteurId;
    private final ObjectMapper objectMapper;
    private final JpaEntrepotRepository jpaEntrepotRepository;
    @Value("${mqtt.topic}")
    private String mqttTopic;

    // 900 000 ms = 15 minutes
    // Pour tests, mettre 30000 (30 secondes)
    // 5 heures = 18 000 000 ms
    @Scheduled(fixedRate = 60000)
    public void simulateSensorData() {
        resolveCapteurId();
        if (capteurId == null) {
            log.warn("Simulation ignorée: aucun capteur trouvé pour l'entrepôt {}", capteurId);
            return;
        }

        Map<String, Object> payloadMap = new LinkedHashMap<>();
        payloadMap.put("temperature", randomInRange(26.0, 33.0));
        payloadMap.put("humidite", randomInRange(52.0, 58.0));
        payloadMap.put("dateMesure", LocalDateTime.now());
        payloadMap.put("idCapteur", capteurId);

        try {
            String payload = objectMapper.writeValueAsString(payloadMap);
            mqttGateway.sendToMqtt(payload, mqttTopic);
            log.info("Donnée simulée envoyée au broker: {}", payload);
        } catch (JsonProcessingException e) {
            log.error("Impossible de sérialiser la donnée simulée MQTT", e);
        }
    }
    private void resolveCapteurId() {
        capteurId = jpaCapteurRepository.findAllByEntrepotId(
                jpaEntrepotRepository.findAll()
                        .getFirst()
                        .getIdEntrepot()
        )
                .getFirst()
                .getIdCapteur();

        log.debug("Capteur ID utilisé pour la simulation: " + capteurId);
    }

    private BigDecimal randomInRange(double min, double max) {
        return BigDecimal.valueOf(min + (Math.random() * (max - min))).setScale(2, RoundingMode.HALF_UP);
    }
}