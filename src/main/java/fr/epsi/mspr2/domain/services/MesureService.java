package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.models.NouvelleMesure;
import fr.epsi.mspr2.domain.ports.MesurePort;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class MesureService {

    private final MesurePort mesurePort;
    private final AlerteService alerteService;
    private final Clock clock;

    /**
     * Enregistre la mesure d'un capteur puis vérifie si elle déclenche une alerte.
     * La mesure reste enregistrée même si l'évaluation de l'alerte échoue.
     */
    public Mesure createMesure(NouvelleMesure nouvelleMesure) {
        if (nouvelleMesure.idCapteur() == null) {
            throw new IllegalArgumentException("idCapteur est obligatoire");
        }
        if (nouvelleMesure.temperature() == null || nouvelleMesure.humidite() == null) {
            throw new IllegalArgumentException("La mesure doit contenir temperature et humidite");
        }

        Mesure mesure = Mesure.builder()
                .temperature(nouvelleMesure.temperature())
                .humidite(nouvelleMesure.humidite())
                .dateMesure(nouvelleMesure.dateMesure() != null ? nouvelleMesure.dateMesure() : LocalDateTime.now(clock))
                .capteur(Capteur.builder().idCapteur(nouvelleMesure.idCapteur()).build())
                .build();

        Mesure enregistree = mesurePort.save(mesure);
        alerteService.evaluerMesure(enregistree);
        return enregistree;
    }

    public Optional<Mesure> getMesureById(UUID idMesure) {
        return mesurePort.findById(idMesure);
    }

    /** @return la mesure supprimée, ou vide si elle n'existe pas */
    public Optional<Mesure> deleteMesure(UUID idMesure) {
        Optional<Mesure> mesure = mesurePort.findById(idMesure);
        mesure.ifPresent(m -> mesurePort.deleteById(idMesure));
        return mesure;
    }
}
