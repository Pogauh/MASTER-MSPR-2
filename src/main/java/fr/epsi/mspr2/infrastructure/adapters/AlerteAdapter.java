package fr.epsi.mspr2.infrastructure.adapters;

import fr.epsi.mspr2.domain.models.*;
import fr.epsi.mspr2.domain.ports.AlertePort;
import fr.epsi.mspr2.infrastructure.jpa.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
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
    private final JpaHistoriqueRepository jpaHistoriqueRepository;
    private final JpaLotRepository jpaLotRepository;
    private final JpaMesureRepository jpaMesureRepository;

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
            JpaCapteurRepository jpaCapteurRepository,
            JpaHistoriqueRepository jpaHistoriqueRepository
        , JpaLotRepository jpaLotRepository, JpaMesureRepository jpaMesureRepository
    ) {
        this.jpaAlerteRepository = jpaAlerteRepository;
        this.jpaCapteurRepository = jpaCapteurRepository;
        this.jpaHistoriqueRepository = jpaHistoriqueRepository;
        this.jpaLotRepository = jpaLotRepository;
        this.jpaMesureRepository = jpaMesureRepository;
    }

    @Override
    @Transactional
    public void sendAlerte(Mesure mesure) {

        List<Mesure> dernieresMesures = jpaMesureRepository.findTop5ByCapteurIdCapteurOrderByDateMesureDesc(mesure.getCapteur().getIdCapteur());

        if (dernieresMesures.size() < 5) {
            return;
        }

        boolean toutesEnAnomalie = dernieresMesures.stream().allMatch(this::isCompatible);

        if (!toutesEnAnomalie) {
            return;
        }

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

        Alerte alerteSauvegardee = jpaAlerteRepository.save(alerte);

        UUID idZoneAffectee = capteur.getZone().getIdZone();

        List<Lot> lotsImpactes = jpaLotRepository.findByZoneIdZone(idZoneAffectee);

        for (Lot lot : lotsImpactes) {
            lot.setStatus(StatusEnum.ALERT);
            Historique historique = new Historique();
            historique.setType(TypeHistoriqueEnum.ALERTE);
            historique.setDateEmission(LocalDateTime.now());
            historique.setZonePrecedente(idZoneAffectee);
            historique.setZoneActuelle(idZoneAffectee);
            historique.setDetail(alerteSauvegardee.getIdAlerte().toString());
            historique.setLot(lot);

            jpaLotRepository.save(lot);
            jpaHistoriqueRepository.save(historique);
        }
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

    @Scheduled(cron = "0 0 1 * * ?")
    public void checkLotExpired() {
        var lots = jpaLotRepository.findAll();
        lots.stream()
                .filter(this::booleanLotExpired)
                .forEach(this::sendAlertExpiredLot);
    }

    private boolean booleanLotExpired(Lot lot) {
        if (lot.getDateStockage() == null) {
            return false;
        }
        return lot.getDateStockage().isBefore(LocalDateTime.now().minusYears(1))&& lot.getStatus()!= StatusEnum.EXPIRED;
    }

    private void sendAlertExpiredLot(Lot lot) {
        Alerte alerte = new Alerte();
        alerte.setTypeAlerte(TypeAlerteEnum.EXPIRED);
        alerte.setDescription("Le lot " + lot.getIdLot() + " a expiré.");
        alerte.setDateEmission(LocalDateTime.now());

        jpaAlerteRepository.save(alerte);

        lot.setStatus(StatusEnum.EXPIRED);
        Historique historique = new Historique();
        historique.setType(TypeHistoriqueEnum.ALERTE);
        historique.setDateEmission(LocalDateTime.now());
        historique.setZonePrecedente(lot.getZone().getIdZone());
        historique.setZoneActuelle(lot.getZone().getIdZone());
        historique.setDetail("Lot expiré");
        historique.setLot(lot);

        jpaLotRepository.save(lot);
        jpaHistoriqueRepository.save(historique);
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