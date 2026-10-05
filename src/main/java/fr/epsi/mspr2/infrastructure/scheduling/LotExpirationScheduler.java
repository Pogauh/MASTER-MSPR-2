package fr.epsi.mspr2.infrastructure.scheduling;

import fr.epsi.mspr2.domain.services.ExpirationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Déclenche chaque nuit la détection des lots périmés. */
@Component
@RequiredArgsConstructor
@Slf4j
public class LotExpirationScheduler {

    private final ExpirationService expirationService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void verifierLotsExpires() {
        int nombreDeLots = expirationService.expirerLotsPerimes();
        log.info("Vérification des lots périmés terminée : {} lot(s) expiré(s)", nombreDeLots);
    }
}
