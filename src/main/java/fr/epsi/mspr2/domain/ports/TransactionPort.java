package fr.epsi.mspr2.domain.ports;

import java.util.function.Supplier;

/**
 * Délimite une unité de travail atomique : tout ce qui est fait dans l'action est validé ensemble,
 * ou annulé ensemble si l'action échoue.
 */
public interface TransactionPort {

    <T> T execute(Supplier<T> action);

    default void execute(Runnable action) {
        execute(() -> {
            action.run();
            return null;
        });
    }
}
