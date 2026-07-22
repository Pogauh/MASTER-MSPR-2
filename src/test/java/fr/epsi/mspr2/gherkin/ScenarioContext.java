package fr.epsi.mspr2.gherkin;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Contexte partagé entre les classes de steps Cucumber dans le même scénario.
 */
@Component
public class ScenarioContext {
    public UUID currentLotId;
    public String nouveauStatut;
}

