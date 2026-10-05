package fr.epsi.mspr2;

import fr.epsi.mspr2.domain.models.SeuilsClimatiques;
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
import fr.epsi.mspr2.infrastructure.messaging.mqtt.MqttMesureListener;
import fr.epsi.mspr2.infrastructure.messaging.mqtt.SensorSimulator;
import fr.epsi.mspr2.infrastructure.scheduling.LotExpirationScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.config.CronTask;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/** L'application complète est correctement assemblée à partir des ports du domaine et de leurs adaptateurs. */
class WiringIT extends IntegrationTestBase {

    @Autowired
    ApplicationContext context;
    @Autowired
    ScheduledAnnotationBeanPostProcessor scheduling;

    @Test
    void chaqueServiceDuDomaineEstUnBeanSimpleNonProxifie() {
        Stream.of(AlerteService.class, CapteurService.class, EntrepotService.class, ExpirationService.class,
                        ExploitationService.class, HistoriqueService.class, LotService.class, MesureService.class,
                        ReportingService.class, ZoneService.class)
                .forEach(type -> {
                    Object service = context.getBean(type);
                    assertThat(AopUtils.isAopProxy(service)).as(type.getSimpleName()).isFalse();
                });
    }

    @Test
    void lesSeuilsDAlerteViennentDeLaConfiguration() {
        assertThat(context.getBean(SeuilsClimatiques.class)).isEqualTo(new SeuilsClimatiques(
                new BigDecimal("29"), new BigDecimal("55"), new BigDecimal("3"), new BigDecimal("2")));
    }

    @Test
    void laVerificationDesLotsPerimesEstPlanifieeChaqueNuit() {
        assertThat(context.getBeansOfType(LotExpirationScheduler.class)).hasSize(1);
        assertThat(scheduling.getScheduledTasks())
                .filteredOn(task -> task.getTask() instanceof CronTask)
                .extracting(task -> ((CronTask) task.getTask()).getExpression())
                .contains("0 0 1 * * ?");
    }

    @Test
    void lEcouteurMqttEstPresent_maisPasLeBrokerNiLeSimulateurEnTest() {
        assertThat(context.getBeansOfType(MqttMesureListener.class)).hasSize(1);
        assertThat(context.getBeansOfType(MqttPahoMessageHandler.class)).isEmpty();
        assertThat(context.getBeansOfType(SensorSimulator.class)).isEmpty();
        assertThat(context.containsBean("inbound")).isFalse();
        assertThat(context.containsBean("mqttInputChannel")).isTrue();
    }
}
