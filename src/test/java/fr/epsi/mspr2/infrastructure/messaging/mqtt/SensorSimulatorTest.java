package fr.epsi.mspr2.infrastructure.messaging.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import fr.epsi.mspr2.domain.ports.CapteurPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorSimulatorTest {

    private static final String TOPIC = "futurekawa/test/data";

    // même configuration Jackson que Spring Boot : les dates sont écrites au format ISO et non en tableau de nombres
    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).build();

    @Mock
    MqttGateway mqttGateway;
    @Mock
    CapteurPort capteurPort;

    private SensorSimulator simulateur(String pays) {
        return new SensorSimulator(mqttGateway, capteurPort, objectMapper, TOPIC, pays);
    }

    @ParameterizedTest(name = "{0} : {1}-{2} °C, {3}-{4} %")
    @CsvSource({
            "bresil, 25, 33, 52, 58",
            "equateur, 26, 35, 57, 63",
            "colombie, 22, 30, 77, 83"
    })
    void chaqueCapteurPublieUneMesureDansLesPlagesDuPays(String pays, double tempMin, double tempMax, double humiMin, double humiMax) throws Exception {
        UUID premier = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        when(capteurPort.findAllIds()).thenReturn(List.of(premier, second));
        SensorSimulator simulateur = simulateur(pays);

        for (int i = 0; i < 100; i++) {
            simulateur.simulateSensorData();
        }

        ArgumentCaptor<String> payloads = ArgumentCaptor.forClass(String.class);
        verify(mqttGateway, times(200)).sendToMqtt(payloads.capture(), eq(TOPIC));
        for (String payload : payloads.getAllValues()) {
            JsonNode mesure = objectMapper.readTree(payload);
            assertThat(mesure.fieldNames()).toIterable().containsExactly("idCapteur", "temperature", "humidite", "dateMesure");
            assertThat(UUID.fromString(mesure.get("idCapteur").asText())).isIn(premier, second);
            assertThat(mesure.get("temperature").asDouble()).isBetween(tempMin, tempMax);
            assertThat(mesure.get("humidite").asDouble()).isBetween(humiMin, humiMax);
            assertThat(LocalDateTime.parse(mesure.get("dateMesure").asText())).isBeforeOrEqualTo(LocalDateTime.now());
        }
    }

    @Test
    void lesValeursSontArrondiesAuCentieme() throws Exception {
        when(capteurPort.findAllIds()).thenReturn(List.of(UUID.randomUUID()));
        SensorSimulator simulateur = simulateur("bresil");

        for (int i = 0; i < 50; i++) {
            simulateur.simulateSensorData();
        }

        ArgumentCaptor<String> payloads = ArgumentCaptor.forClass(String.class);
        verify(mqttGateway, times(50)).sendToMqtt(payloads.capture(), anyString());
        for (String payload : payloads.getAllValues()) {
            assertThat(objectMapper.readTree(payload).get("temperature").decimalValue().scale()).isLessThanOrEqualTo(2);
            assertThat(objectMapper.readTree(payload).get("humidite").decimalValue().scale()).isLessThanOrEqualTo(2);
        }
    }

    @Test
    void unPaysInconnu_neSimulePasDeValeurs() throws Exception {
        UUID capteur = UUID.randomUUID();
        when(capteurPort.findAllIds()).thenReturn(List.of(capteur));

        simulateur("atlantide").simulateSensorData();

        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        verify(mqttGateway).sendToMqtt(payload.capture(), eq(TOPIC));
        assertThat(objectMapper.readTree(payload.getValue()).fieldNames()).toIterable().containsExactly("idCapteur", "dateMesure");
    }

    @Test
    void sansCapteur_rienNEstPublie() {
        when(capteurPort.findAllIds()).thenReturn(List.of());

        simulateur("bresil").simulateSensorData();

        verifyNoInteractions(mqttGateway);
    }
}
