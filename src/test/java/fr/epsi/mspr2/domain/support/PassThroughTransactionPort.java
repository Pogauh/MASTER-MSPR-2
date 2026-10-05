package fr.epsi.mspr2.domain.support;

import fr.epsi.mspr2.domain.ports.TransactionPort;

import java.util.function.Supplier;

/** Transaction factice pour les tests unitaires : exécute l'action telle quelle et compte les unités de travail ouvertes. */
public class PassThroughTransactionPort implements TransactionPort {

    private int executions;
    private boolean enCours;

    @Override
    public <T> T execute(Supplier<T> action) {
        executions++;
        enCours = true;
        try {
            return action.get();
        } finally {
            enCours = false;
        }
    }

    public int executions() {
        return executions;
    }

    /** @return {@code true} pendant l'exécution d'une action, pour vérifier ce qui se passe dans ou hors transaction */
    public boolean enCours() {
        return enCours;
    }
}
