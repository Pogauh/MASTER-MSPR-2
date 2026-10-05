package fr.epsi.mspr2.infrastructure.scheduling;

import fr.epsi.mspr2.domain.services.ExpirationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LotExpirationSchedulerTest {

    @Mock
    ExpirationService expirationService;

    @Test
    void verifierLotsExpires_declencheLExpirationDesLotsPerimes() {
        when(expirationService.expirerLotsPerimes()).thenReturn(2);

        new LotExpirationScheduler(expirationService).verifierLotsExpires();

        verify(expirationService).expirerLotsPerimes();
    }

    @Test
    void laVerificationEstPlanifieeChaqueNuitAUneHeure() throws NoSuchMethodException {
        Scheduled planification = LotExpirationScheduler.class.getMethod("verifierLotsExpires").getAnnotation(Scheduled.class);

        assertThat(planification.cron()).isEqualTo("0 0 1 * * ?");
    }
}
