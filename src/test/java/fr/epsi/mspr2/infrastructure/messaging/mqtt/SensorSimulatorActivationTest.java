package fr.epsi.mspr2.infrastructure.messaging.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import fr.epsi.mspr2.domain.ports.CapteurPort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Le simulateur est actif par défaut et se désactive avec {@code app.simulator.enabled=false} ;
 * {@code app.simulator.excluded-capteurs} liste (séparés par des virgules) les capteurs réels à ne pas simuler.
 */
class SensorSimulatorActivationTest {

    private final MqttGateway gateway = mock(MqttGateway.class);
    private final CapteurPort capteurPort = mock(CapteurPort.class);

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(SensorSimulator.class)
            .withBean(MqttGateway.class, () -> gateway)
            .withBean(CapteurPort.class, () -> capteurPort)
            .withBean(ObjectMapper.class, () -> Jackson2ObjectMapperBuilder.json()
                    .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).build())
            .withPropertyValues("mqtt.topic=futurekawa/test/data", "app.apiName=bresil");

    @Test
    void actifParDefaut() {
        runner.run(context -> assertThat(context).hasSingleBean(SensorSimulator.class));
    }

    @Test
    void actifQuandOnLeDemande() {
        runner.withPropertyValues("app.simulator.enabled=true")
                .run(context -> assertThat(context).hasSingleBean(SensorSimulator.class));
    }

    @Test
    void lesCapteursExclusSontLusDepuisLaConfigurationSepareesParDesVirgules() {
        UUID reel1 = UUID.randomUUID();
        UUID reel2 = UUID.randomUUID();
        UUID simule = UUID.randomUUID();
        when(capteurPort.findAllIds()).thenReturn(List.of(reel1, simule, reel2));

        runner.withPropertyValues("app.simulator.excluded-capteurs=" + reel1 + ", " + reel2)
                .run(context -> {
                    context.getBean(SensorSimulator.class).simulateSensorData();

                    ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
                    verify(gateway).sendToMqtt(payload.capture(), eq("futurekawa/test/data"));
                    assertThat(payload.getValue()).contains(simule.toString())
                            .doesNotContain(reel1.toString()).doesNotContain(reel2.toString());
                });
    }

    @Test
    void sansExclusionTousLesCapteursSontSimules() {
        when(capteurPort.findAllIds()).thenReturn(List.of(UUID.randomUUID(), UUID.randomUUID()));

        runner.withPropertyValues("app.simulator.excluded-capteurs=")
                .run(context -> {
                    context.getBean(SensorSimulator.class).simulateSensorData();

                    verify(gateway, times(2)).sendToMqtt(anyString(), eq("futurekawa/test/data"));
                });
    }

    @Test
    void desactivable() {
        runner.withPropertyValues("app.simulator.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(SensorSimulator.class));
    }
}
