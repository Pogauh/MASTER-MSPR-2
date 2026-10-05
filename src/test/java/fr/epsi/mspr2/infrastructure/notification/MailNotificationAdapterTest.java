package fr.epsi.mspr2.infrastructure.notification;

import fr.epsi.mspr2.domain.models.Alerte;
import fr.epsi.mspr2.domain.models.TypeAlerteEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MailNotificationAdapterTest {

    @Mock
    JavaMailSender mailSender;

    private Alerte alerte() {
        return Alerte.builder()
                .idAlerte(UUID.fromString("c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1"))
                .typeAlerte(TypeAlerteEnum.HAUTETEMPERATURE)
                .description("Temperature trop haute: 35.00 (ideal=29, tolerance=3)")
                .dateEmission(LocalDateTime.of(2026, 3, 15, 10, 0))
                .build();
    }

    @Test
    void notifierAlerte_envoieUnMailAuResponsable() {
        MailNotificationAdapter adapter = new MailNotificationAdapter(mailSender, "responsable@futurekawa.test", "alertes@futurekawa.test");

        adapter.notifierAlerte(alerte());

        ArgumentCaptor<SimpleMailMessage> mail = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(mail.capture());
        assertThat(mail.getValue().getFrom()).isEqualTo("alertes@futurekawa.test");
        assertThat(mail.getValue().getTo()).containsExactly("responsable@futurekawa.test");
        assertThat(mail.getValue().getSubject()).isEqualTo("ALERTE SYSTÈME : HAUTETEMPERATURE");
        assertThat(mail.getValue().getText()).isEqualTo(
                "Bonjour,\n\nUne alerte a été levée.\n\n"
                        + "Détails :\n"
                        + "- Identifiant de l'alerte : c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1\n"
                        + "- Type : HAUTETEMPERATURE\n"
                        + "- Date d'émission : 2026-03-15T10:00\n"
                        + "- Description : Temperature trop haute: 35.00 (ideal=29, tolerance=3)\n\n"
                        + "Cordialement,\nSystème de Gestion Automatique.");
    }

    @Test
    void notifierAlerte_unEchecDEnvoiNeFaitPasEchouerLAppelant() {
        MailNotificationAdapter adapter = new MailNotificationAdapter(mailSender, "responsable@futurekawa.test", "alertes@futurekawa.test");
        doThrow(new MailSendException("SMTP indisponible")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> adapter.notifierAlerte(alerte())).doesNotThrowAnyException();
    }
}
