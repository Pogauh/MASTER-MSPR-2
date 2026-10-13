package fr.epsi.mspr2.gherkin;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Étapes communes : préparation de la base (Contexte) et vérifications partagées par plusieurs fonctionnalités. */
public class ReferentielSteps {

    private static final List<String> TABLES_DES_ENFANTS_AUX_PARENTS =
            List.of("historique", "alerte", "mesure", "lot", "capteur", "zone", "exploitation", "entrepot");

    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    ScenarioContext contexte;
    @Autowired
    JavaMailSender mailSender;

    @Before
    public void repartirDUneBaseVide() {
        TABLES_DES_ENFANTS_AUX_PARENTS.forEach(table -> jdbc.update("delete from " + table));
        Mockito.reset(mailSender);
    }

    @Given("une exploitation {string}")
    public void une_exploitation(String nom) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into exploitation (id_exploitation, nom, adresse) values (?, ?, ?)", id, nom, "804 rue du Barista");
        contexte.exploitation(nom, id);
    }

    @Given("un entrepôt {string} avec une zone {string}")
    public void un_entrepot_avec_une_zone(String nomEntrepot, String nomZone) {
        UUID idEntrepot = UUID.randomUUID();
        UUID idZone = UUID.randomUUID();
        jdbc.update("insert into entrepot (id_entrepot, nom, localisation) values (?, ?, ?)", idEntrepot, nomEntrepot, "Bresil");
        jdbc.update("insert into zone (id_zone, nom, id_entrepot) values (?, ?, ?)", idZone, nomZone, idEntrepot);
        contexte.entrepot(nomEntrepot, idEntrepot);
        contexte.zone(nomZone, idZone);
    }

    @Given("la zone {string} dans l'entrepôt {string}")
    public void la_zone_dans_l_entrepot(String nomZone, String nomEntrepot) {
        UUID idZone = UUID.randomUUID();
        jdbc.update("insert into zone (id_zone, nom, id_entrepot) values (?, ?, ?)", idZone, nomZone, contexte.entrepot(nomEntrepot));
        contexte.zone(nomZone, idZone);
    }

    @Given("un capteur surveille la zone {string}")
    public void un_capteur_surveille_la_zone(String nomZone) {
        UUID idCapteur = UUID.randomUUID();
        jdbc.update("insert into capteur (id_capteur, code_serie, id_zone) values (?, ?, ?)",
                idCapteur, "02:AA:BB:CC:DD:" + idCapteur.toString().substring(0, 8), contexte.zone(nomZone));
        contexte.capteurDeLaZone(nomZone, idCapteur);
    }

    @Given("un lot {string} conforme de {int} kg stocké dans la zone {string}")
    public void un_lot_conforme_stocke_dans_la_zone(String nomLot, int quantite, String nomZone) {
        insererLot(nomLot, "COMPLIANT", quantite, LocalDateTime.now().minusDays(30), null, nomZone);
    }

    @Given("un lot {string} conforme stocké depuis {int} jours dans la zone {string}")
    public void un_lot_conforme_stocke_depuis_dans_la_zone(String nomLot, int jours, String nomZone) {
        insererLot(nomLot, "COMPLIANT", 100, LocalDateTime.now().minusDays(jours), null, nomZone);
    }

    @Given("un lot {string} au statut {string} stocké depuis {int} jours dans la zone {string}")
    public void un_lot_au_statut_stocke_depuis_dans_la_zone(String nomLot, String statut, int jours, String nomZone) {
        insererLot(nomLot, statut, 100, LocalDateTime.now().minusDays(jours), null, nomZone);
    }

    @Given("un lot {string} produit il y a {int} jours et stocké depuis {int} jours dans la zone {string}")
    public void un_lot_produit_il_y_a_et_stocke_depuis_dans_la_zone(String nomLot, int joursDepuisProduction, int joursDepuisStockage, String nomZone) {
        insererLot(nomLot, "COMPLIANT", 100, LocalDateTime.now().minusDays(joursDepuisStockage), LocalDate.now().minusDays(joursDepuisProduction), nomZone);
    }

    @Then("le lot {string} a le statut {string}")
    public void le_lot_a_le_statut(String nomLot, String statut) {
        Assertions.assertEquals(statut, jdbc.queryForObject("select status from lot where id_lot = ?", String.class, contexte.lot(nomLot)));
    }

    @Then("le lot {string} est dans la zone {string}")
    public void le_lot_est_dans_la_zone(String nomLot, String nomZone) {
        Assertions.assertEquals(contexte.zone(nomZone),
                jdbc.queryForObject("select id_zone from lot where id_lot = ?", UUID.class, contexte.lot(nomLot)));
    }

    private void insererLot(String nomLot, String statut, int quantite, LocalDateTime stockage, LocalDate production, String nomZone) {
        UUID idLot = UUID.randomUUID();
        UUID exploitation = contexte.exploitation("Flamenco");
        jdbc.update("""
                        insert into lot (id_lot, status, quantite, caracteristique, date_stockage, date_production, id_exploitation, id_zone)
                        values (?, ?, ?, ?, ?, ?, ?, ?)""",
                idLot, statut, quantite, 2, Timestamp.valueOf(stockage), production == null ? null : Date.valueOf(production),
                exploitation, contexte.zone(nomZone));
        contexte.lot(nomLot, idLot);
    }
}
