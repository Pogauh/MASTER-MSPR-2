package fr.epsi.mspr2.domain.ports;

import fr.epsi.mspr2.domain.models.Alerte;

/** Prévient le responsable de l'entrepôt qu'une alerte vient d'être levée. */
public interface NotificationPort {

    /** Ne doit jamais faire échouer l'appelant : une notification qui n'aboutit pas est simplement journalisée. */
    void notifierAlerte(Alerte alerte);
}
