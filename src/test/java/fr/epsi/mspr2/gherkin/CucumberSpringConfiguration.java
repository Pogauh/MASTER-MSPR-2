package fr.epsi.mspr2.gherkin;

import fr.epsi.mspr2.api.controller.*;
import fr.epsi.mspr2.domain.services.*;
import fr.epsi.mspr2.infrastructure.adapters.LotAdapter;
import fr.epsi.mspr2.infrastructure.adapters.MesureAdapter;
import fr.epsi.mspr2.infrastructure.jpa.*;
import fr.epsi.mspr2.infrastructure.mapper.*;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;

@CucumberContextConfiguration
@WebMvcTest(controllers = {
        LotControlleur.class,
        MesureController.class,
        AlerteController.class,
        ReportingController.class,
        HistoriqueController.class,
        EntrepotController.class,
        ZoneControlleur.class,
        ExploitationController.class,
        CapteurController.class
})
@Import({
        LotService.class,
        LotAdapter.class,
        LotMapperImpl.class,
        MesureMapperImpl.class,
        HistoriqueMapperImpl.class,
        MesureService.class,
        MesureAdapter.class,
        AlerteService.class,
        AlerteMapperImpl.class,
        ReportingMapperImpl.class,
        ZoneMapperImpl.class,
        CapteurMapperImpl.class
})
public class CucumberSpringConfiguration {

    // ── Repositories ──────────────────────────────────────────────────────────
    @MockBean
    private JpaLotRepository jpaLotRepository;

    @MockBean
    private JpaMesureRepository jpaMesureRepository;

    @MockBean
    private JpaHistoriqueRepository historiqueJpaRepository;

    @MockBean
    private JpaAlerteRepository jpaAlerteRepository;

    @MockBean
    private JpaCapteurRepository jpaCapteurRepository;

    @MockBean
    private JpaEntrepotRepository jpaEntrepotRepository;

    @MockBean
    private JpaZoneRepository jpaZoneRepository;

    @MockBean
    private JpaExploitationRepository jpaExploitationRepository;

    // ── Services mockés (pour ne pas démarrer la logique métier réelle) ──────
    @MockBean
    private HistoriqueService historiqueService;

    @MockBean
    private ReportingService reportingService;

    @MockBean
    private EntrepotService entrepotService;

    @MockBean
    private ZoneService zoneService;

    @MockBean
    private ExploitationService exploitationService;

    @MockBean
    private CapteurService capteurService;

    // ── Infrastructure ────────────────────────────────────────────────────────
    @MockBean
    private JavaMailSender javaMailSender;
}
