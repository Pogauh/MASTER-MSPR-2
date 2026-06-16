package fr.epsi.mspr2.domain.simulateur;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.epsi.mspr2.domain.models.Capteur;
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
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class SensorSimulator {

    private final MqttGateway mqttGateway;
    private final JpaCapteurRepository jpaCapteurRepository;

    private List<UUID> capteurId;
    private final ObjectMapper objectMapper;
    private final JpaEntrepotRepository jpaEntrepotRepository;
    @Value("${mqtt.topic}")
    private String mqttTopic;

    @Value("${app.apiName}")
    private String apiName;

    // 900 000 ms = 15 minutes
    // Pour tests, mettre 30000 (30 secondes)
    // 5 heures = 18 000 000 ms
    @Scheduled(fixedRate = 60000)
    public void simulateSensorData() {
        resolveCapteurId();
        if (capteurId == null) {
            log.warn("Simulation ignorée: liste de capteurId vide ");
            return;
        }

        capteurId.stream().forEach(id -> {

            Map<String, Object> payloadMap = new LinkedHashMap<>();
            payloadMap.put("idCapteur", id);
            if (Objects.equals(apiName, "bresil")){
                payloadMap.put("temperature", randomInRange(25.0, 33.0));
                payloadMap.put("humidite", randomInRange(52.0, 58.0));
            }
            if (Objects.equals(apiName, "equateur")){
                payloadMap.put("temperature", randomInRange(26.0, 35.0));
                payloadMap.put("humidite", randomInRange(57.0, 63.0));
            }
            if (Objects.equals(apiName, "colombie")){
                payloadMap.put("temperature", randomInRange(22.0, 30.0));
                payloadMap.put("humidite", randomInRange(77.0, 83.0));
            }
            payloadMap.put("dateMesure", LocalDateTime.now());

            try {
                String payload = objectMapper.writeValueAsString(payloadMap);
                mqttGateway.sendToMqtt(payload, mqttTopic);
                log.info("Donnée simulée envoyée au broker: {}", payload);
            } catch (JsonProcessingException e) {
                log.error("Impossible de sérialiser la donnée simulée MQTT", e);
            }
        });
    }
    private void resolveCapteurId() {
        capteurId = jpaCapteurRepository.findAll()
                        .stream().map(Capteur::getIdCapteur)
                        .toList();

        log.debug("Capteur ID utilisé pour la simulation: " + capteurId);
    }

    private BigDecimal randomInRange(double min, double max) {
        return BigDecimal.valueOf(min + (Math.random() * (max - min))).setScale(2, RoundingMode.HALF_UP);
    }
}