package fr.epsi.mspr2.infrastructure.persistence.adapter;

import fr.epsi.mspr2.domain.ports.TransactionPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * Exécute les actions du domaine dans une transaction Spring : les adaptateurs de persistance appelés
 * pendant l'action y participent, et tout est annulé si elle lève une exception.
 */
@Component
public class TransactionAdapter implements TransactionPort {

    private final TransactionTemplate transactionTemplate;

    public TransactionAdapter(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T execute(Supplier<T> action) {
        return transactionTemplate.execute(status -> action.get());
    }
}
