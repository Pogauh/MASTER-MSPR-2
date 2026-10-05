package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Alerte;
import fr.epsi.mspr2.domain.models.AlerteDetails;
import fr.epsi.mspr2.domain.models.Anomalie;
import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.models.SeuilsClimatiques;
import fr.epsi.mspr2.domain.ports.AlertePort;
import fr.epsi.mspr2.domain.ports.CapteurPort;
import fr.epsi.mspr2.domain.ports.HistoriquePort;
import fr.epsi.mspr2.domain.ports.LotPort;
import fr.epsi.mspr2.domain.ports.MesurePort;
import fr.epsi.mspr2.domain.ports.NotificationPort;
import fr.epsi.mspr2.domain.ports.TransactionPort;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class AlerteService {

    /** Nombre de mesures consécutives hors tolérance nécessaires pour lever une alerte. */
    static final int NB_MESURES_CONSECUTIVES_HORS_PLAGE = 5;

    private final AlertePort alertePort;
    private final MesurePort mesurePort;
    private final CapteurPort capteurPort;
    private final LotPort lotPort;
    private final HistoriquePort historiquePort;
    private final NotificationPort notificationPort;
    private final TransactionPort transactionPort;
    private final SeuilsClimatiques seuils;
    private final Clock clock;

    /**
     * Lève une alerte quand la mesure, ainsi que les quatre précédentes du même capteur, sortent
     * de la plage idéale. L'alerte place les lots de la zone du capteur en alerte et prévient le responsable.
     */
    public void evaluerMesure(Mesure mesure) {
        if (!seuils.estHorsPlage(mesure)) {
            return;
        }

        UUID idCapteur = mesure.getCapteur() != null ? mesure.getCapteur().getIdCapteur() : null;
        if (idCapteur == null) {
            throw new IllegalArgumentException("L'id du capteur est obligatoire pour evaluer une alerte");
        }

        List<Mesure> dernieresMesures = mesurePort.findLatestByCapteurId(idCapteur, NB_MESURES_CONSECUTIVES_HORS_PLAGE);
        if (dernieresMesures.size() < NB_MESURES_CONSECUTIVES_HORS_PLAGE
                || !dernieresMesures.stream().allMatch(seuils::estHorsPlage)) {
            return;
        }

        Capteur capteur = capteurPort.findById(idCapteur)
                .orElseThrow(() -> new IllegalStateException("Capteur introuvable pour l'id " + idCapteur));
        Optional<Anomalie> anomalie = seuils.detecterAnomalie(mesure);
        if (anomalie.isEmpty()) {
            return;
        }

        Alerte alerte = transactionPort.execute(() -> enregistrerAlerte(capteur, anomalie.get()));
        notificationPort.notifierAlerte(alerte);
    }

    public List<AlerteDetails> getAlerteByEntrepot(UUID idEntrepot, LocalDateTime startDate, LocalDateTime endDate) {
        return alertePort.findDetailsByEntrepotId(idEntrepot, startDate, endDate);
    }

    private Alerte enregistrerAlerte(Capteur capteur, Anomalie anomalie) {
        LocalDateTime maintenant = LocalDateTime.now(clock);
        Alerte alerte = alertePort.save(Alerte.builder()
                .typeAlerte(anomalie.type())
                .description(anomalie.description())
                .dateEmission(maintenant)
                .capteur(capteur)
                .build());

        for (Lot lot : lotPort.findByZoneId(capteur.getZone().getIdZone())) {
            lot.passerEnAlerte();
            lotPort.save(lot);
            historiquePort.save(Historique.alerte(lot, alerte.getIdAlerte().toString(), maintenant));
        }
        return alerte;
    }
}
