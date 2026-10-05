package fr.epsi.mspr2.gherkin;

import io.cucumber.spring.CucumberContextConfiguration;
import io.cucumber.spring.ScenarioScope;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

/** Les scénarios s'exécutent sur l'application complète (REST, domaine, JPA/H2), sans broker MQTT ni vrai SMTP. */
@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CucumberSpringConfiguration {

    @MockBean
    JavaMailSender mailSender;

    @TestConfiguration
    static class ScenarioBeans {

        @Bean
        @ScenarioScope
        ScenarioContext scenarioContext() {
            return new ScenarioContext();
        }
    }
}
