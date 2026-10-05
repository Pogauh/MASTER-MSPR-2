package fr.epsi.mspr2.infrastructure.config;

import fr.epsi.mspr2.domain.models.SeuilsClimatiques;
import fr.epsi.mspr2.domain.ports.AlertePort;
import fr.epsi.mspr2.domain.ports.CapteurPort;
import fr.epsi.mspr2.domain.ports.EntrepotPort;
import fr.epsi.mspr2.domain.ports.ExploitationPort;
import fr.epsi.mspr2.domain.ports.HistoriquePort;
import fr.epsi.mspr2.domain.ports.LotPort;
import fr.epsi.mspr2.domain.ports.MesurePort;
import fr.epsi.mspr2.domain.ports.NotificationPort;
import fr.epsi.mspr2.domain.ports.TransactionPort;
import fr.epsi.mspr2.domain.services.AlerteService;
import fr.epsi.mspr2.domain.services.CapteurService;
import fr.epsi.mspr2.domain.services.EntrepotService;
import fr.epsi.mspr2.domain.services.ExpirationService;
import fr.epsi.mspr2.domain.services.ExploitationService;
import fr.epsi.mspr2.domain.services.HistoriqueService;
import fr.epsi.mspr2.domain.services.LotService;
import fr.epsi.mspr2.domain.services.MesureService;
import fr.epsi.mspr2.domain.services.ReportingService;
import fr.epsi.mspr2.domain.services.ZoneService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Assemble le domaine : les services métier ne connaissent pas Spring, c'est cette configuration
 * qui les instancie en leur fournissant les adaptateurs qui réalisent les ports.
 */
@Configuration
@EnableConfigurationProperties(SeuilsAlerteProperties.class)
public class DomainConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public SeuilsClimatiques seuilsClimatiques(SeuilsAlerteProperties properties) {
        return properties.toSeuils();
    }

    @Bean
    public AlerteService alerteService(AlertePort alertePort, MesurePort mesurePort, CapteurPort capteurPort,
                                       LotPort lotPort, HistoriquePort historiquePort, NotificationPort notificationPort,
                                       TransactionPort transactionPort, SeuilsClimatiques seuils, Clock clock) {
        return new AlerteService(alertePort, mesurePort, capteurPort, lotPort, historiquePort, notificationPort,
                transactionPort, seuils, clock);
    }

    @Bean
    public MesureService mesureService(MesurePort mesurePort, AlerteService alerteService, Clock clock) {
        return new MesureService(mesurePort, alerteService, clock);
    }

    @Bean
    public LotService lotService(LotPort lotPort, HistoriquePort historiquePort, MesurePort mesurePort,
                                 TransactionPort transactionPort, Clock clock) {
        return new LotService(lotPort, historiquePort, mesurePort, transactionPort, clock);
    }

    @Bean
    public ExpirationService expirationService(LotPort lotPort, AlertePort alertePort, HistoriquePort historiquePort,
                                               NotificationPort notificationPort, TransactionPort transactionPort,
                                               Clock clock) {
        return new ExpirationService(lotPort, alertePort, historiquePort, notificationPort, transactionPort, clock);
    }

    @Bean
    public HistoriqueService historiqueService(HistoriquePort historiquePort) {
        return new HistoriqueService(historiquePort);
    }

    @Bean
    public CapteurService capteurService(MesurePort mesurePort) {
        return new CapteurService(mesurePort);
    }

    @Bean
    public ZoneService zoneService(MesurePort mesurePort) {
        return new ZoneService(mesurePort);
    }

    @Bean
    public EntrepotService entrepotService(EntrepotPort entrepotPort, CapteurPort capteurPort, MesurePort mesurePort,
                                           Clock clock) {
        return new EntrepotService(entrepotPort, capteurPort, mesurePort, clock);
    }

    @Bean
    public ExploitationService exploitationService(ExploitationPort exploitationPort) {
        return new ExploitationService(exploitationPort);
    }

    @Bean
    public ReportingService reportingService(LotPort lotPort, HistoriquePort historiquePort, AlertePort alertePort,
                                             Clock clock) {
        return new ReportingService(lotPort, historiquePort, alertePort, clock);
    }
}
