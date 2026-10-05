package fr.epsi.mspr2.infrastructure.config;

import fr.epsi.mspr2.domain.models.SeuilsClimatiques;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeuilsAlertePropertiesTest {

    private static final String PREFIX = "app.alert.tolerences";

    @Test
    void lesProprietesSontLuesDepuisLaConfiguration() {
        Binder binder = new Binder(new MapConfigurationPropertySource(Map.of(
                PREFIX + ".temperature-ideale", "29",
                PREFIX + ".humidite-ideale", "55",
                PREFIX + ".tolerance-temperature", "3",
                PREFIX + ".tolerance-humidite", "2.5")));

        SeuilsAlerteProperties properties = binder.bind(PREFIX, SeuilsAlerteProperties.class).get();

        assertThat(properties.toSeuils()).isEqualTo(new SeuilsClimatiques(
                new BigDecimal("29"), new BigDecimal("55"), new BigDecimal("3"), new BigDecimal("2.5")));
    }

    @Test
    void uneProprieteManquanteEmpecheLeDemarrage() {
        Binder binder = new Binder(new MapConfigurationPropertySource(Map.of(
                PREFIX + ".temperature-ideale", "29",
                PREFIX + ".humidite-ideale", "55",
                PREFIX + ".tolerance-temperature", "3")));

        assertThatThrownBy(() -> binder.bind(PREFIX, SeuilsAlerteProperties.class))
                .hasRootCauseInstanceOf(NullPointerException.class)
                .hasStackTraceContaining("app.alert.tolerences.tolerance-humidite est obligatoire");
    }
}
