package fr.epsi.mspr2.containers;

import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Rejoue un test d'intégration sur un vrai PostgreSQL au lieu de H2.
 * Le test est ignoré quand Docker n'est pas disponible sur la machine.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Testcontainers(disabledWithoutDocker = true)
@ContextConfiguration(initializers = PostgresInitializer.class)
public @interface OnPostgres {
}
