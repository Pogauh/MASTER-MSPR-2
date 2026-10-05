package fr.epsi.mspr2.infrastructure.notification;

import fr.epsi.mspr2.domain.models.Alerte;
import fr.epsi.mspr2.domain.ports.NotificationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** Prévient le responsable de l'entrepôt par e-mail. Un échec d'envoi est journalisé et ne remonte pas. */
@Component
@Slf4j
public class MailNotificationAdapter implements NotificationPort {

    private final JavaMailSender mailSender;
    private final String emailResponsable;
    private final String emailExpediteur;

    public MailNotificationAdapter(JavaMailSender mailSender,
                                   @Value("${app.mail.responsable}") String emailResponsable,
                                   @Value("${app.mail.from:ultrabaga@hotmail.com}") String emailExpediteur) {
        this.mailSender = mailSender;
        this.emailResponsable = emailResponsable;
        this.emailExpediteur = emailExpediteur;
    }

    @Override
    public void notifierAlerte(Alerte alerte) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailExpediteur);
            message.setTo(emailResponsable);
            message.setSubject("ALERTE SYSTÈME : " + alerte.getTypeAlerte());

            String corpsMail = String.format(
                    "Bonjour,\n\nUne alerte a été levée.\n\n" +
                            "Détails :\n" +
                            "- Identifiant de l'alerte : %s\n" +
                            "- Type : %s\n" +
                            "- Date d'émission : %s\n" +
                            "- Description : %s\n\n" +
                            "Cordialement,\nSystème de Gestion Automatique.",
                    alerte.getIdAlerte(),
                    alerte.getTypeAlerte(),
                    alerte.getDateEmission(),
                    alerte.getDescription()
            );

            message.setText(corpsMail);
            mailSender.send(message);
            log.info("E-mail d'alerte envoyé avec succès à : {}", emailResponsable);
        } catch (Exception e) {
            log.error("Échec de l'envoi de l'e-mail pour l'alerte ID : {}", alerte.getIdAlerte(), e);
        }
    }
}
