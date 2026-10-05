package fr.epsi.mspr2;

import fr.epsi.mspr2.infrastructure.messaging.mqtt.MqttGateway;
import fr.epsi.mspr2.infrastructure.messaging.mqtt.SensorSimulator;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Chaîne MQTT complète avec un vrai broker Mosquitto :
 * capteur -> broker -> {@code MqttMesureListener} -> base, et simulateur -> broker -> écouteur -> base.
 * Ignoré si Docker n'est pas disponible.
 */
@Testcontainers(disabledWithoutDocker = true)
// le contexte est fermé tant que le broker tourne encore : l'arrêt des clients MQTT est alors immédiat
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(properties = {"mqtt.enabled=true", "app.simulator.enabled=true", "mqtt.topic=futurekawa/test/data"})
@ActiveProfiles("test")
class MqttBrokerIT {

    private static final String TOPIC = "futurekawa/test/data";
    private static final LocalDateTime DATE_DU_MESSAGE = LocalDateTime.of(2026, 3, 15, 16, 0);
    private static final LocalDateTime DEBUT_DE_LA_SIMULATION = LocalDateTime.now().minusMinutes(1);

    @Container
    static final GenericContainer<?> MOSQUITTO = new GenericContainer<>("eclipse-mosquitto:2")
            .withExposedPorts(1883)
            .withCopyFileToContainer(MountableFile.forClasspathResource("mosquitto-test.conf"), "/mosquitto/config/mosquitto.conf")
            .waitingFor(Wait.forLogMessage(".*running.*", 1));

    @DynamicPropertySource
    static void broker(DynamicPropertyRegistry registry) {
        registry.add("mqtt.broker-uri", () -> brokerUri());
    }

    private static String brokerUri() {
        return "tcp://" + MOSQUITTO.getHost() + ":" + MOSQUITTO.getMappedPort(1883);
    }

    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    SensorSimulator simulator;
    @Autowired
    MqttGateway gateway;
    @MockBean
    JavaMailSender mailSender;

    private MqttClient client;
    private final UUID capteur = UUID.randomUUID();

    @BeforeEach
    void seed() throws MqttException {
        for (String table : List.of("historique", "alerte", "mesure", "lot", "capteur", "zone", "exploitation", "entrepot")) {
            jdbc.update("delete from " + table);
        }
        UUID entrepot = UUID.randomUUID();
        UUID zone = UUID.randomUUID();
        jdbc.update("insert into entrepot (id_entrepot, localisation, nom) values (?, ?, ?)", entrepot, "Bresil", "Principal");
        jdbc.update("insert into zone (id_zone, nom, id_entrepot) values (?, ?, ?)", zone, "A", entrepot);
        jdbc.update("insert into capteur (id_capteur, code_serie, id_zone) values (?, ?, ?)", capteur, "02:AA:BB:CC:DD:01", zone);

        client = new MqttClient(brokerUri(), "test-client-" + UUID.randomUUID(), new MemoryPersistence());
        client.connect();
    }

    @AfterEach
    void disconnect() throws MqttException {
        if (client != null && client.isConnected()) {
            client.disconnect();
        }
        client.close();
    }

    /** Le simulateur peut publier à tout moment pour ce capteur : on ne regarde que les mesures de la date voulue. */
    private List<Map<String, Object>> mesuresDuCapteurALaDate(LocalDateTime date) {
        return jdbc.queryForList("select * from mesure where id_capteur = ? and date_mesure = ?", capteur, Timestamp.valueOf(date));
    }

    private List<Map<String, Object>> mesuresDuCapteurDepuis(LocalDateTime date) {
        return jdbc.queryForList("select * from mesure where id_capteur = ? and date_mesure >= ?", capteur, Timestamp.valueOf(date));
    }

    @Test
    void unMessagePublieParUnCapteurSurLeBroker_estEnregistreEnBase() {
        String payload = "{\"temperature\":27.50,\"humidite\":56.20,\"dateMesure\":\"2026-03-15T16:00:00\",\"idCapteur\":\"%s\"}".formatted(capteur);

        // l'abonnement du backend au broker est asynchrone : on republie jusqu'à ce que le message arrive
        await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofMillis(500)).untilAsserted(() -> {
            client.publish(TOPIC, new MqttMessage(payload.getBytes(StandardCharsets.UTF_8)));
            assertThat(mesuresDuCapteurALaDate(DATE_DU_MESSAGE)).isNotEmpty();
        });

        Map<String, Object> mesure = mesuresDuCapteurALaDate(DATE_DU_MESSAGE).get(0);
        assertThat((BigDecimal) mesure.get("temperature")).isEqualByComparingTo("27.50");
        assertThat((BigDecimal) mesure.get("humidite")).isEqualByComparingTo("56.20");
        assertThat(((Timestamp) mesure.get("date_mesure")).toLocalDateTime()).isEqualTo(DATE_DU_MESSAGE);
    }

    @Test
    void leSimulateurPublieSurLeBroker_etLeBackendEnregistreLaMesure() {
        // le simulateur publie une mesure par capteur ; elle revient par le broker jusqu'à l'écouteur
        await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofMillis(500)).untilAsserted(() -> {
            simulator.simulateSensorData();
            assertThat(mesuresDuCapteurDepuis(DEBUT_DE_LA_SIMULATION)).isNotEmpty();
        });

        Map<String, Object> mesure = mesuresDuCapteurDepuis(DEBUT_DE_LA_SIMULATION).get(0);
        assertThat(((BigDecimal) mesure.get("temperature")).doubleValue()).isBetween(25.0, 33.0);
        assertThat(((BigDecimal) mesure.get("humidite")).doubleValue()).isBetween(52.0, 58.0);
    }

    @Test
    void lePasserelleSortanteEnvoieSurLeTopicDemande() throws MqttException {
        List<String> recus = new CopyOnWriteArrayList<>();
        client.subscribe("futurekawa/test/sortie", (topic, message) -> recus.add(new String(message.getPayload(), StandardCharsets.UTF_8)));

        await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofMillis(500)).untilAsserted(() -> {
            gateway.sendToMqtt("{\"hello\":\"world\"}", "futurekawa/test/sortie");
            assertThat(recus).contains("{\"hello\":\"world\"}");
        });
    }
}
