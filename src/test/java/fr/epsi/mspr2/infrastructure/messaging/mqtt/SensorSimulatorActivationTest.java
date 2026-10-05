package fr.epsi.mspr2.infrastructure.messaging.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.epsi.mspr2.domain.ports.CapteurPort;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/** Le simulateur est actif par défaut et se désactive avec {@code app.simulator.enabled=false}. */
class SensorSimulatorActivationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(SensorSimulator.class)
            .withBean(MqttGateway.class, () -> mock(MqttGateway.class))
            .withBean(CapteurPort.class, () -> mock(CapteurPort.class))
            .withBean(ObjectMapper.class, ObjectMapper::new)
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
    void desactivable() {
        runner.withPropertyValues("app.simulator.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(SensorSimulator.class));
    }
}
