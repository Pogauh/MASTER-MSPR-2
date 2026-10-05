package fr.epsi.mspr2.domain.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static fr.epsi.mspr2.domain.support.Fixtures.SEUILS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Seuils utilisés : température 29 ± 3 (26 à 32), humidité 55 ± 2 (53 à 57). */
class SeuilsClimatiquesTest {

    private static Mesure mesure(String temperature, String humidite) {
        return Mesure.builder().temperature(new BigDecimal(temperature)).humidite(new BigDecimal(humidite)).build();
    }

    @ParameterizedTest(name = "{0} °C / {1} % est dans la plage")
    @CsvSource({"29, 55", "26, 55", "32, 55", "29, 53", "29, 57", "26.00, 53.00", "31.99, 56.99"})
    void estHorsPlage_laissePasserLesValeursDeLaPlageBornesIncluses(String temperature, String humidite) {
        assertThat(SEUILS.estHorsPlage(mesure(temperature, humidite))).isFalse();
    }

    @ParameterizedTest(name = "{0} °C / {1} % est hors plage")
    @CsvSource({"32.01, 55", "25.99, 55", "29, 57.01", "29, 52.99", "40, 70", "0, 0"})
    void estHorsPlage_detecteUneTemperatureOuUneHumiditeHorsPlage(String temperature, String humidite) {
        assertThat(SEUILS.estHorsPlage(mesure(temperature, humidite))).isTrue();
    }

    @Test
    void estHorsPlage_exigeLaTemperatureEtLHumidite() {
        Mesure sansTemperature = Mesure.builder().humidite(new BigDecimal("55")).build();
        Mesure sansHumidite = Mesure.builder().temperature(new BigDecimal("29")).build();

        assertThatThrownBy(() -> SEUILS.estHorsPlage(sansTemperature))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La mesure doit contenir temperature et humidite");
        assertThatThrownBy(() -> SEUILS.estHorsPlage(sansHumidite))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La mesure doit contenir temperature et humidite");
    }

    @Test
    void detecterAnomalie_temperatureTropHaute() {
        assertThat(SEUILS.detecterAnomalie(mesure("35.00", "55.00"))).contains(
                new Anomalie(TypeAlerteEnum.HAUTETEMPERATURE, "Temperature trop haute: 35.00 (ideal=29, tolerance=3)"));
    }

    @Test
    void detecterAnomalie_temperatureTropBasse() {
        assertThat(SEUILS.detecterAnomalie(mesure("20.00", "55.00"))).contains(
                new Anomalie(TypeAlerteEnum.BASSETEMPERATURE, "Temperature trop basse: 20.00 (ideal=29, tolerance=3)"));
    }

    @Test
    void detecterAnomalie_humiditeTropHaute() {
        assertThat(SEUILS.detecterAnomalie(mesure("29.00", "70.00"))).contains(
                new Anomalie(TypeAlerteEnum.HAUTEHUMIDITE, "Humidite trop haute: 70.00 (ideal=55, tolerance=2)"));
    }

    @Test
    void detecterAnomalie_humiditeTropBasse() {
        assertThat(SEUILS.detecterAnomalie(mesure("29.00", "40.00"))).contains(
                new Anomalie(TypeAlerteEnum.BASSEHUMIDITE, "Humidite trop basse: 40.00 (ideal=55, tolerance=2)"));
    }

    @Test
    void detecterAnomalie_laTemperatureEstExamineeAvantLHumidite() {
        assertThat(SEUILS.detecterAnomalie(mesure("40", "70")))
                .map(Anomalie::type).contains(TypeAlerteEnum.HAUTETEMPERATURE);
        assertThat(SEUILS.detecterAnomalie(mesure("20", "70")))
                .map(Anomalie::type).contains(TypeAlerteEnum.BASSETEMPERATURE);
    }

    @Test
    void detecterAnomalie_rienADeclarerDansLaPlage() {
        assertThat(SEUILS.detecterAnomalie(mesure("32", "57"))).isEmpty();
        assertThat(SEUILS.detecterAnomalie(mesure("26", "53"))).isEmpty();
    }

    @Test
    void detecterAnomalie_ignoreLesValeursAbsentes() {
        assertThat(SEUILS.detecterAnomalie(Mesure.builder().build())).isEmpty();
    }

    @Test
    void laDescriptionReprendLesValeursDeConfigurationTellesQuelles() {
        SeuilsClimatiques seuils = new SeuilsClimatiques(
                new BigDecimal("26.5"), new BigDecimal("80"), new BigDecimal("1.5"), new BigDecimal("2.0"));

        assertThat(seuils.detecterAnomalie(mesure("30", "80")))
                .map(Anomalie::description)
                .contains("Temperature trop haute: 30 (ideal=26.5, tolerance=1.5)");
    }
}
