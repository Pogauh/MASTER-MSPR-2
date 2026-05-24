package fr.epsi.mspr2.gherkin;

import fr.epsi.mspr2.api.controller.LotControlleur;
import fr.epsi.mspr2.domain.services.LotService;
import fr.epsi.mspr2.infrastructure.adapters.LotAdapter;
import fr.epsi.mspr2.infrastructure.jpa.JpaLotRepository;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@CucumberContextConfiguration
@WebMvcTest(controllers = LotControlleur.class)
@Import({LotService.class, LotAdapter.class})
public class CucumberSpringConfiguration {
    @MockBean
    private JpaLotRepository jpaLotRepository;
}
