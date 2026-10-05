package fr.epsi.mspr2.infrastructure.persistence;

import fr.epsi.mspr2.domain.models.StatusEnum;
import fr.epsi.mspr2.domain.models.TypeAlerteEnum;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.infrastructure.persistence.entity.AlerteEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.CapteurEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.EntrepotEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.ExploitationEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.HistoriqueEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.LotEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.MesureEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.ZoneEntity;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Insère directement en base des entités pour préparer les scénarios des tests de persistance. */
public class PersistenceFixtures {

    private final TestEntityManager em;

    public PersistenceFixtures(TestEntityManager em) {
        this.em = em;
    }

    public EntrepotEntity entrepot(String nom) {
        EntrepotEntity entrepot = new EntrepotEntity();
        entrepot.setNom(nom);
        entrepot.setLocalisation("Bresil");
        return em.persist(entrepot);
    }

    public ZoneEntity zone(EntrepotEntity entrepot, String nom) {
        ZoneEntity zone = new ZoneEntity();
        zone.setNom(nom);
        zone.setEntrepot(entrepot);
        return em.persist(zone);
    }

    public CapteurEntity capteur(ZoneEntity zone) {
        CapteurEntity capteur = new CapteurEntity();
        capteur.setCodeSerie("02:AA:BB:CC:DD:" + UUID.randomUUID().toString().substring(0, 8));
        capteur.setZone(zone);
        return em.persist(capteur);
    }

    public ExploitationEntity exploitation() {
        ExploitationEntity exploitation = new ExploitationEntity();
        exploitation.setNom("Exploitation Flamenco");
        exploitation.setAdresse("804 rue du Barista");
        return em.persist(exploitation);
    }

    public LotEntity lot(ZoneEntity zone, ExploitationEntity exploitation, StatusEnum status, int quantite,
                         LocalDateTime dateStockage, LocalDate dateProduction) {
        LotEntity lot = new LotEntity();
        lot.setZone(zone);
        lot.setExploitation(exploitation);
        lot.setStatus(status);
        lot.setQuantite(quantite);
        lot.setCaracteristique(2);
        lot.setDateStockage(dateStockage);
        lot.setDateProduction(dateProduction);
        return em.persist(lot);
    }

    public MesureEntity mesure(CapteurEntity capteur, String temperature, String humidite, LocalDateTime date) {
        MesureEntity mesure = new MesureEntity();
        mesure.setCapteur(capteur);
        mesure.setTemperature(new BigDecimal(temperature));
        mesure.setHumidite(new BigDecimal(humidite));
        mesure.setDateMesure(date);
        return em.persist(mesure);
    }

    public HistoriqueEntity historique(LotEntity lot, TypeHistoriqueEnum type, LocalDateTime date, UUID zonePrecedente, UUID zoneActuelle) {
        HistoriqueEntity historique = new HistoriqueEntity();
        historique.setLot(lot);
        historique.setType(type);
        historique.setDateEmission(date);
        historique.setZonePrecedente(zonePrecedente);
        historique.setZoneActuelle(zoneActuelle);
        historique.setDetail("détail " + type);
        return em.persist(historique);
    }

    public AlerteEntity alerte(CapteurEntity capteur, TypeAlerteEnum type, LocalDateTime date) {
        AlerteEntity alerte = new AlerteEntity();
        alerte.setCapteur(capteur);
        alerte.setTypeAlerte(type);
        alerte.setDescription("alerte " + type);
        alerte.setDateEmission(date);
        return em.persist(alerte);
    }

    /** Force l'écriture en base puis vide le cache : les lectures suivantes relisent réellement la base. */
    public void flushAndClear() {
        em.flush();
        em.clear();
    }
}
