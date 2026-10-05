package fr.epsi.mspr2.infrastructure.persistence;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

/** Ajoute au contexte {@code @DataJpaTest} les adaptateurs et les mappers de la couche de persistance. */
@TestConfiguration
@ComponentScan(basePackages = {
        "fr.epsi.mspr2.infrastructure.persistence.adapter",
        "fr.epsi.mspr2.infrastructure.persistence.mapper"})
class PersistenceTestConfig {
}
