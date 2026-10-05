package fr.epsi.mspr2.domain.models;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Conditions de conservation du pays : valeurs idéales et tolérances admises
 * pour la température et l'humidité.
 */
public record SeuilsClimatiques(BigDecimal temperatureIdeale, BigDecimal humiditeIdeale,
                                BigDecimal toleranceTemperature, BigDecimal toleranceHumidite) {

    private static final String IDEAL = " (ideal=";
    private static final String TOLERANCE = ", tolerance=";

    /** @return {@code true} si la température ou l'humidité sort de la plage idéale ± tolérance */
    public boolean estHorsPlage(Mesure mesure) {
        if (mesure.getTemperature() == null || mesure.getHumidite() == null) {
            throw new IllegalArgumentException("La mesure doit contenir temperature et humidite");
        }
        return mesure.getTemperature().compareTo(temperatureMin()) < 0
                || mesure.getTemperature().compareTo(temperatureMax()) > 0
                || mesure.getHumidite().compareTo(humiditeMin()) < 0
                || mesure.getHumidite().compareTo(humiditeMax()) > 0;
    }

    /** Qualifie l'écart de la mesure ; la température est examinée avant l'humidité. */
    public Optional<Anomalie> detecterAnomalie(Mesure mesure) {
        BigDecimal temperature = mesure.getTemperature();
        BigDecimal humidite = mesure.getHumidite();

        if (temperature != null && temperature.compareTo(temperatureMax()) > 0) {
            return Optional.of(new Anomalie(TypeAlerteEnum.HAUTETEMPERATURE,
                    "Temperature trop haute: " + temperature + IDEAL + temperatureIdeale + TOLERANCE + toleranceTemperature + ")"));
        }
        if (temperature != null && temperature.compareTo(temperatureMin()) < 0) {
            return Optional.of(new Anomalie(TypeAlerteEnum.BASSETEMPERATURE,
                    "Temperature trop basse: " + temperature + IDEAL + temperatureIdeale + TOLERANCE + toleranceTemperature + ")"));
        }
        if (humidite != null && humidite.compareTo(humiditeMax()) > 0) {
            return Optional.of(new Anomalie(TypeAlerteEnum.HAUTEHUMIDITE,
                    "Humidite trop haute: " + humidite + IDEAL + humiditeIdeale + TOLERANCE + toleranceHumidite + ")"));
        }
        if (humidite != null && humidite.compareTo(humiditeMin()) < 0) {
            return Optional.of(new Anomalie(TypeAlerteEnum.BASSEHUMIDITE,
                    "Humidite trop basse: " + humidite + IDEAL + humiditeIdeale + TOLERANCE + toleranceHumidite + ")"));
        }
        return Optional.empty();
    }

    private BigDecimal temperatureMin() {
        return temperatureIdeale.subtract(toleranceTemperature);
    }

    private BigDecimal temperatureMax() {
        return temperatureIdeale.add(toleranceTemperature);
    }

    private BigDecimal humiditeMin() {
        return humiditeIdeale.subtract(toleranceHumidite);
    }

    private BigDecimal humiditeMax() {
        return humiditeIdeale.add(toleranceHumidite);
    }
}
