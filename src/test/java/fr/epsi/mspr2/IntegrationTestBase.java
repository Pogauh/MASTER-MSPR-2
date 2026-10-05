package fr.epsi.mspr2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

/**
 * Socle des tests d'intégration : application complète (REST, domaine, JPA) sur une base H2 en mémoire,
 * sans broker MQTT ni simulateur, et avec un expéditeur d'e-mails factice.
 * Toutes les classes qui en héritent partagent le même contexte Spring.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    private static final List<String> TABLES_DES_ENFANTS_AUX_PARENTS =
            List.of("historique", "alerte", "mesure", "lot", "capteur", "zone", "exploitation", "entrepot");

    @Autowired
    protected JdbcTemplate jdbc;

    @MockBean
    protected JavaMailSender mailSender;

    protected void viderLaBase() {
        TABLES_DES_ENFANTS_AUX_PARENTS.forEach(table -> jdbc.update("delete from " + table));
    }
}
