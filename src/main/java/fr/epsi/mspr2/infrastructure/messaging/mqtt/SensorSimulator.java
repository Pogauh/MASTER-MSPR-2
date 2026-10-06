package fr.epsi.mspr2.infrastructure.messaging.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.epsi.mspr2.domain.ports.CapteurPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Publie périodiquement sur MQTT des mesures simulées pour chaque capteur, dans les plages
 * de température et d'humidité propres au pays ({@code app.apiName}).
 * Désactivable avec {@code app.simulator.enabled=false}.
 *
 * <p>Les capteurs listés dans {@code app.simulator.excluded-capteurs} (séparés par des virgules) ne sont jamais simulés :
 * ce sont les capteurs réels (par exemple l'ESP32 du module IoT), dont les mesures ne doivent pas se mélanger aux fausses.
 */
@Component
@ConditionalOnProperty(name = "app.simulator.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class SensorSimulator {

    /** Plages de valeurs simulées. */
    record Plage(double temperatureMin, double temperatureMax, double humiditeMin, double humiditeMax) {
    }

    private static final Map<String, Plage> PLAGES_PAR_PAYS = Map.of(
            "bresil", new Plage(25.0, 33.0, 52.0, 58.0),
            "equateur", new Plage(26.0, 35.0, 57.0, 63.0),
            "colombie", new Plage(22.0, 30.0, 77.0, 83.0));

    private final MqttGateway mqttGateway;
    private final CapteurPort capteurPort;
    private final ObjectMapper objectMapper;
    private final String mqttTopic;
    private final String apiName;
    private final Set<UUID> capteursExclus;

    public SensorSimulator(MqttGateway mqttGateway, CapteurPort capteurPort, ObjectMapper objectMapper,
                           @Value("${mqtt.topic}") String mqttTopic,
                           @Value("${app.apiName}") String apiName,
                           @Value("${app.simulator.excluded-capteurs:}") String capteursExclus) {
        this.mqttGateway = mqttGateway;
        this.capteurPort = capteurPort;
        this.objectMapper = objectMapper;
        this.mqttTopic = mqttTopic;
        this.apiName = apiName;
        this.capteursExclus = parseCapteurs(capteursExclus);
    }

    /**
     * Lit une liste d'identifiants de capteurs séparés par des virgules (espaces tolérés, éléments vides ignorés).
     *
     * @throws IllegalArgumentException si un élément n'est pas un UUID : mieux vaut échouer au démarrage que simuler
     *                                  par erreur un capteur réel
     */
    static Set<UUID> parseCapteurs(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(valeur.split(","))
                .map(String::trim)
                .filter(element -> !element.isEmpty())
                .map(element -> {
                    try {
                        return UUID.fromString(element);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException(
                                "app.simulator.excluded-capteurs : « " + element + " » n'est pas un identifiant de capteur (UUID)", e);
                    }
                })
                .collect(Collectors.toUnmodifiableSet());
    }

    // 900 000 ms = 15 minutes
    // Pour tests, mettre 30000 (30 secondes)
    // 5 heures = 18 000 000 ms
    @Scheduled(fixedRate = 60000)
    public void simulateSensorData() {
        List<UUID> capteurIds = capteurPort.findAllIds().stream()
                .filter(idCapteur -> !capteursExclus.contains(idCapteur))
                .toList();
        log.debug("Capteurs utilisés pour la simulation: {} (exclus : {})", capteurIds, capteursExclus);

        capteurIds.forEach(this::publierMesureSimulee);
    }

    private void publierMesureSimulee(UUID idCapteur) {
        Map<String, Object> payloadMap = new LinkedHashMap<>();
        payloadMap.put("idCapteur", idCapteur);
        Plage plage = PLAGES_PAR_PAYS.get(apiName);
        if (plage != null) {
            payloadMap.put("temperature", randomInRange(plage.temperatureMin(), plage.temperatureMax()));
            payloadMap.put("humidite", randomInRange(plage.humiditeMin(), plage.humiditeMax()));
        }
        payloadMap.put("dateMesure", LocalDateTime.now());

        try {
            String payload = objectMapper.writeValueAsString(payloadMap);
            mqttGateway.sendToMqtt(payload, mqttTopic);
            log.info("Donnée simulée envoyée au broker: {}", payload);
        } catch (JsonProcessingException e) {
            log.error("Impossible de sérialiser la donnée simulée MQTT", e);
        }
    }

    private BigDecimal randomInRange(double min, double max) {
        return BigDecimal.valueOf(min + (Math.random() * (max - min))).setScale(2, RoundingMode.HALF_UP);
    }
}
