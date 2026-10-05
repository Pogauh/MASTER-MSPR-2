package fr.epsi.mspr2.domain.models;

/** Écart constaté entre une mesure et les valeurs idéales de conservation. */
public record Anomalie(TypeAlerteEnum type, String description) {
}
