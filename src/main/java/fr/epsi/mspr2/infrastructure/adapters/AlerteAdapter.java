package fr.epsi.mspr2.infrastructure.adapters;

import fr.epsi.mspr2.domain.models.*;
import fr.epsi.mspr2.domain.ports.AlertePort;
import fr.epsi.mspr2.infrastructure.jpa.JpaAlerteRepository;
import fr.epsi.mspr2.infrastructure.jpa.JpaCapteurRepository;
import fr.epsi.mspr2.infrastructure.jpa.JpaGenereAlerteRepository;
import fr.epsi.mspr2.infrastructure.jpa.JpaLotRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Component
public class AlerteAdapter implements AlertePort {

    private final JpaAlerteRepository jpaAlerteRepository;
    private final JpaCapteurRepository jpaCapteurRepository;
    private final JpaLotRepository jpaLotRepository;
    private final JpaGenereAlerteRepository jpaGenereAlerteRepository;


    @Override
    @Transactional
    public void sendAlerte(Mesure mesure) {
        if (!isCompatible(mesure)) {
            return;
        }

        Capteur capteur = resolveCapteurWithPays(mesure);
        Pays pays = capteur.getEntrepot().getPays();

        BigDecimal temp = mesure.getTemperature();
        BigDecimal humi = mesure.getHumidite();
        BigDecimal tempIdeal = pays.getTempIdeale();
        BigDecimal humiIdeal = pays.getHumiIdeale();
        BigDecimal tempTolerance = defaultTolerance(pays.getToleranceTemp());
        BigDecimal humiTolerance = defaultTolerance(pays.getToleranceHumi());

        BigDecimal tempMin = tempIdeal.subtract(tempTolerance);
        BigDecimal tempMax = tempIdeal.add(tempTolerance);
        BigDecimal humiMin = humiIdeal.subtract(humiTolerance);
        BigDecimal humiMax = humiIdeal.add(humiTolerance);

        Alerte alerte = new Alerte();
        if (temp.compareTo(tempMax) > 0) {
            alerte.setTypeAlerte(TypeAlerteEnum.HAUTETEMPERATURE);
            alerte.setDescription("Temperature trop haute: " + temp + " (ideal=" + tempIdeal + ", tolerance=" + tempTolerance + ")");
        } else if (temp.compareTo(tempMin) < 0) {
            alerte.setTypeAlerte(TypeAlerteEnum.BASSETEMPERATURE);
            alerte.setDescription("Temperature trop basse: " + temp + " (ideal=" + tempIdeal + ", tolerance=" + tempTolerance + ")");
        } else if (humi.compareTo(humiMax) > 0) {
            alerte.setTypeAlerte(TypeAlerteEnum.HAUTEHUMIDITE);
            alerte.setDescription("Humidite trop haute: " + humi + " (ideal=" + humiIdeal + ", tolerance=" + humiTolerance + ")");
        } else if (humi.compareTo(humiMin) < 0) {
            alerte.setTypeAlerte(TypeAlerteEnum.BASSEHUMIDITE);
            alerte.setDescription("Humidite trop basse: " + humi + " (ideal=" + humiIdeal + ", tolerance=" + humiTolerance + ")");
        }
        alerte.setDateEmission(LocalDateTime.now());
        Alerte savedAlerte = jpaAlerteRepository.save(alerte);
        createLinksWithLots(savedAlerte, capteur);
    }

    @Override
    public boolean isCompatible(Mesure mesure) {
        Capteur capteur = resolveCapteurWithPays(mesure);
        Pays pays = capteur.getEntrepot().getPays();

        if (pays.getTempIdeale() == null || pays.getHumiIdeale() == null) {
            throw new IllegalStateException("Les valeurs temp_ideale et humi_ideale doivent etre renseignees pour le pays " + pays.getIdPays());
        }
        if (mesure.getTemperature() == null || mesure.getHumidite() == null) {
            throw new IllegalArgumentException("La mesure doit contenir temperature et humidite");
        }

        BigDecimal tempTolerance = defaultTolerance(pays.getToleranceTemp());
        BigDecimal humiTolerance = defaultTolerance(pays.getToleranceHumi());

        BigDecimal tempMin = pays.getTempIdeale().subtract(tempTolerance);
        BigDecimal tempMax = pays.getTempIdeale().add(tempTolerance);
        BigDecimal humiMin = pays.getHumiIdeale().subtract(humiTolerance);
        BigDecimal humiMax = pays.getHumiIdeale().add(humiTolerance);

        boolean temperatureOutOfRange = mesure.getTemperature().compareTo(tempMin) < 0
            || mesure.getTemperature().compareTo(tempMax) > 0;
        boolean humiditeOutOfRange = mesure.getHumidite().compareTo(humiMin) < 0
            || mesure.getHumidite().compareTo(humiMax) > 0;

        return temperatureOutOfRange || humiditeOutOfRange;
    }

    private Capteur resolveCapteurWithPays(Mesure mesure) {
        UUID capteurId = mesure.getCapteur() != null ? mesure.getCapteur().getIdCapteur() : null;
        if (capteurId == null) {
            throw new IllegalArgumentException("L'id du capteur est obligatoire pour evaluer une alerte");
        }

        return jpaCapteurRepository.findByIdWithEntrepotAndPays(capteurId)
            .orElseThrow(() -> new IllegalStateException("Capteur introuvable pour l'id " + capteurId));
    }

    private BigDecimal defaultTolerance(BigDecimal tolerance) {
        return tolerance != null ? tolerance : BigDecimal.ZERO;
    }

    private void createLinksWithLots(Alerte alerte, Capteur capteur) {
        if (alerte.getIdAlerte() == null) {
            return;
        }
        if (capteur.getEntrepot() == null || capteur.getEntrepot().getIdEntrepot() == null) {
            return;
        }
        if (capteur.getEmplacementPrecis() == null || capteur.getEmplacementPrecis().isBlank()) {
            return;
        }

        UUID entrepotId = capteur.getEntrepot().getIdEntrepot();
        String emplacementCapteur = capteur.getEmplacementPrecis();

        List<Lot> lots = jpaLotRepository
            .findAllByEntrepotIdEntrepotAndEmplacementIgnoreCase(entrepotId, emplacementCapteur);

        for (Lot lot : lots) {
            if (lot.getIdLot() == null) {
                continue;
            }
            jpaGenereAlerteRepository.save(new GenereAlerte(lot, alerte));
        }
    }
}
