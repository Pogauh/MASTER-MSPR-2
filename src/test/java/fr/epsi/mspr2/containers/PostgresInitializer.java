package fr.epsi.mspr2.containers;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.ZoneId;

/**
 * Branche le contexte Spring sur un vrai PostgreSQL 15 (même image que le docker-compose du projet).
 * Le conteneur est démarré une seule fois pour toute la session de tests, au premier contexte qui en a besoin.
 */
public class PostgresInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final class Holder {
        // Même fuseau horaire que la JVM : les âges calculés en SQL (CURRENT_TIMESTAMP) restent comparables aux dates écrites par les tests.
        static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine")
                .withDatabaseName("db_test")
                .withUsername("test")
                .withPassword("test")
                .withEnv("TZ", ZoneId.systemDefault().getId())
                .withCommand("postgres", "-c", "timezone=" + ZoneId.systemDefault().getId());

        static {
            POSTGRES.start();
        }
    }

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        PostgreSQLContainer<?> postgres = Holder.POSTGRES;
        TestPropertyValues.of(
                "spring.datasource.url=" + postgres.getJdbcUrl(),
                "spring.datasource.username=" + postgres.getUsername(),
                "spring.datasource.password=" + postgres.getPassword(),
                "spring.datasource.driver-class-name=org.postgresql.Driver",
                "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect"
        ).applyTo(context);
    }
}
