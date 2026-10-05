package fr.epsi.mspr2.infrastructure.config;

import fr.epsi.mspr2.domain.models.SeuilsClimatiques;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Valeurs idéales de conservation du pays et tolérances admises avant de lever une alerte
 * (propriétés {@code app.alert.tolerences.*}).
 */
@ConfigurationProperties(prefix = "app.alert.tolerences")
public record SeuilsAlerteProperties(BigDecimal temperatureIdeale, BigDecimal humiditeIdeale,
                                     BigDecimal toleranceTemperature, BigDecimal toleranceHumidite) {

    public SeuilsAlerteProperties {
        Objects.requireNonNull(temperatureIdeale, "app.alert.tolerences.temperature-ideale est obligatoire");
        Objects.requireNonNull(humiditeIdeale, "app.alert.tolerences.humidite-ideale est obligatoire");
        Objects.requireNonNull(toleranceTemperature, "app.alert.tolerences.tolerance-temperature est obligatoire");
        Objects.requireNonNull(toleranceHumidite, "app.alert.tolerences.tolerance-humidite est obligatoire");
    }

    public SeuilsClimatiques toSeuils() {
        return new SeuilsClimatiques(temperatureIdeale, humiditeIdeale, toleranceTemperature, toleranceHumidite);
    }
}
