package fr.epsi.mspr2.infrastructure.adapters;

import fr.epsi.mspr2.domain.models.*;
import fr.epsi.mspr2.domain.ports.AlertePort;
import fr.epsi.mspr2.infrastructure.jpa.JpaAlerteRepository;
import fr.epsi.mspr2.infrastructure.jpa.JpaCapteurRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class AlerteAdapter implements AlertePort {

    private final JpaAlerteRepository jpaAlerteRepository;
    private final JpaCapteurRepository jpaCapteurRepository;

    @Value("${app.alert.tolerences.temperature-ideale}")
    private BigDecimal tempIdeale;

    @Value("${app.alert.tolerences.humidite-ideale}")
    private BigDecimal humiIdeale;

    @Value("${app.alert.tolerences.tolerance-temperature}")
    private BigDecimal toleranceTemp;

    @Value("${app.alert.tolerences.tolerance-humidite}")
    private BigDecimal toleranceHumi;

    private static final String TOLERANCE_LABEL = ", tolerance=";
    private static final String IDEAL_LABEL = " (ideal=";

    public AlerteAdapter(
        JpaAlerteRepository jpaAlerteRepository,
        JpaCapteurRepository jpaCapteurRepository
    ) {
        this.jpaAlerteRepository = jpaAlerteRepository;
        this.jpaCapteurRepository = jpaCapteurRepository;
    }

    @Override
    @Transactional
    public void sendAlerte(Mesure mesure) {

        Capteur capteur = resolveCapteurWithZone(mesure);
        BigDecimal temp = mesure.getTemperature();
        BigDecimal humi = mesure.getHumidite();
        BigDecimal tempMin = tempIdeale.subtract(toleranceTemp);
        BigDecimal tempMax = tempIdeale.add(toleranceTemp);
        BigDecimal humiMin = humiIdeale.subtract(toleranceHumi);
        BigDecimal humiMax = humiIdeale.add(toleranceHumi);

        Alerte alerte = new Alerte();
        if (temp != null && temp.compareTo(tempMax) > 0) {
            alerte.setTypeAlerte(TypeAlerteEnum.HAUTETEMPERATURE);
            alerte.setDescription("Temperature trop haute: " + temp + IDEAL_LABEL + tempIdeale + TOLERANCE_LABEL + toleranceTemp + ")");
        } else if (temp != null && temp.compareTo(tempMin) < 0) {
            alerte.setTypeAlerte(TypeAlerteEnum.BASSETEMPERATURE);
            alerte.setDescription("Temperature trop basse: " + temp + IDEAL_LABEL + tempIdeale + TOLERANCE_LABEL + toleranceTemp + ")");
        } else if (humi != null && humi.compareTo(humiMax) > 0) {
            alerte.setTypeAlerte(TypeAlerteEnum.HAUTEHUMIDITE);
            alerte.setDescription("Humidite trop haute: " + humi + IDEAL_LABEL + humiIdeale + TOLERANCE_LABEL + toleranceHumi + ")");
        } else if (humi != null && humi.compareTo(humiMin) < 0) {
            alerte.setTypeAlerte(TypeAlerteEnum.BASSEHUMIDITE);
            alerte.setDescription("Humidite trop basse: " + humi + IDEAL_LABEL + humiIdeale + TOLERANCE_LABEL + toleranceHumi + ")");
        }

        if (alerte.getTypeAlerte() == null) {
            return;
        }

        alerte.setDateEmission(LocalDateTime.now());
        alerte.setCapteur(capteur);
        jpaAlerteRepository.save(alerte);
    }

    @Override
    public List<AlerteDetails> findAlerteByEntrepot(UUID idEntrepot, LocalDateTime startDate, LocalDateTime endDate){
        return jpaAlerteRepository.findByEntrepotId(idEntrepot,startDate,endDate);
    }

    @Override
    public boolean isCompatible(Mesure mesure) {
        if (mesure.getTemperature() == null || mesure.getHumidite() == null) {
            throw new IllegalArgumentException("La mesure doit contenir temperature et humidite");
        }

        BigDecimal tempMin = tempIdeale.subtract(toleranceTemp);
        BigDecimal tempMax = tempIdeale.add(toleranceTemp);
        BigDecimal humiMin = humiIdeale.subtract(toleranceHumi);
        BigDecimal humiMax = humiIdeale.add(toleranceHumi);

        boolean temperatureOutOfRange = mesure.getTemperature().compareTo(tempMin) < 0
            || mesure.getTemperature().compareTo(tempMax) > 0;
        boolean humiditeOutOfRange = mesure.getHumidite().compareTo(humiMin) < 0
            || mesure.getHumidite().compareTo(humiMax) > 0;

        return temperatureOutOfRange || humiditeOutOfRange;
    }

    private Capteur resolveCapteurWithZone(Mesure mesure) {
        UUID capteurId = mesure.getCapteur() != null ? mesure.getCapteur().getIdCapteur() : null;
        if (capteurId == null) {
            throw new IllegalArgumentException("L'id du capteur est obligatoire pour evaluer une alerte");
        }

        return jpaCapteurRepository.findByCapteurId(capteurId)
            .orElseThrow(() -> new IllegalStateException("Capteur introuvable pour l'id " + capteurId));
    }
}