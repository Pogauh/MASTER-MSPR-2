package fr.epsi.mspr2.gherkin;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Température idéale 29 ± 3 °C, humidité idéale 55 ± 2 % (configuration par défaut). */
public class AlerteSteps {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    JavaMailSender mailSender;
    @Autowired
    ScenarioContext contexte;

    private int minuteSuivante;

    @When("le capteur de la zone {string} envoie {int} mesures de {int} degrés et {int} % d'humidité")
    public void le_capteur_envoie_des_mesures(String nomZone, int nombre, int temperature, int humidite) throws Exception {
        for (int i = 0; i < nombre; i++) {
            envoyerMesure(nomZone, temperature, humidite);
        }
    }

    @When("le capteur de la zone {string} envoie une mesure de {int} degrés et {int} % d'humidité")
    public void le_capteur_envoie_une_mesure(String nomZone, int temperature, int humidite) throws Exception {
        envoyerMesure(nomZone, temperature, humidite);
    }

    @Then("une alerte {string} est enregistrée pour la zone {string}")
    public void une_alerte_est_enregistree(String type, String nomZone) {
        List<Map<String, Object>> alertes = jdbc.queryForList("select * from alerte where id_capteur = ?", contexte.capteurDeLaZone(nomZone));
        Assertions.assertEquals(1, alertes.size());
        Assertions.assertEquals(type, alertes.get(0).get("type_alerte"));
    }

    @Then("aucune alerte n'est enregistrée")
    public void aucune_alerte_n_est_enregistree() {
        Assertions.assertEquals(0, jdbc.queryForObject("select count(*) from alerte", Integer.class));
    }

    @Then("la description de l'alerte est {string}")
    public void la_description_de_l_alerte_est(String description) {
        Assertions.assertEquals(description, jdbc.queryForObject("select description from alerte", String.class));
    }

    @Then("l'historique du lot {string} mentionne l'alerte")
    public void l_historique_du_lot_mentionne_l_alerte(String nomLot) {
        UUID idAlerte = jdbc.queryForObject("select id_alerte from alerte", UUID.class);
        List<Map<String, Object>> historiques = jdbc.queryForList(
                "select * from historique where id_lot = ? and type = 'ALERTE'", contexte.lot(nomLot));
        Assertions.assertEquals(1, historiques.size());
        Assertions.assertEquals(idAlerte.toString(), historiques.get(0).get("detail"));
    }

    @Then("un e-mail d'alerte est envoyé au responsable")
    public void un_email_d_alerte_est_envoye_au_responsable() {
        ArgumentCaptor<SimpleMailMessage> mail = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(mail.capture());
        Assertions.assertArrayEquals(new String[]{"responsable@futurekawa.test"}, mail.getValue().getTo());
        Assertions.assertTrue(mail.getValue().getSubject().startsWith("ALERTE SYSTÈME : "));
    }

    @Then("aucun e-mail d'alerte n'est envoyé")
    public void aucun_email_d_alerte_n_est_envoye() {
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    private void envoyerMesure(String nomZone, int temperature, int humidite) throws Exception {
        LocalDateTime date = LocalDateTime.of(2026, 3, 15, 10, 0).plusMinutes(minuteSuivante++);
        String corps = "{\"temperature\":%d,\"humidite\":%d,\"dateMesure\":\"%s\",\"idCapteur\":\"%s\"}"
                .formatted(temperature, humidite, date, contexte.capteurDeLaZone(nomZone));
        mockMvc.perform(post("/api/mesures").contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isCreated());
    }
}
