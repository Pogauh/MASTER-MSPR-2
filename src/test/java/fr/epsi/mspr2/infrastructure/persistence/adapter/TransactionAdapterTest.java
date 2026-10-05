package fr.epsi.mspr2.infrastructure.persistence.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionAdapterTest {

    @Mock
    PlatformTransactionManager transactionManager;

    TransactionAdapter adapter;
    final TransactionStatus status = new SimpleTransactionStatus();

    @BeforeEach
    void setUp() {
        when(transactionManager.getTransaction(any())).thenReturn(status);
        adapter = new TransactionAdapter(transactionManager);
    }

    @Test
    void execute_valideLaTransactionEtRenvoieLeResultatDeLAction() {
        String resultat = adapter.execute(() -> "fait");

        assertThat(resultat).isEqualTo("fait");
        verify(transactionManager).commit(status);
        verify(transactionManager, never()).rollback(any());
    }

    @Test
    void execute_sansResultat() {
        AtomicBoolean executee = new AtomicBoolean();

        adapter.execute(() -> executee.set(true));

        assertThat(executee).isTrue();
        verify(transactionManager).commit(status);
    }

    @Test
    void execute_annuleLaTransactionQuandLActionEchoue() {
        assertThatThrownBy(() -> adapter.execute(() -> {
            throw new IllegalStateException("échec");
        })).isInstanceOf(IllegalStateException.class).hasMessage("échec");

        verify(transactionManager).rollback(status);
        verify(transactionManager, never()).commit(any());
    }
}
