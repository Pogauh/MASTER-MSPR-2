package fr.epsi.mspr2.gherkin;

import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** État partagé entre les étapes d'un même scénario : identifiants des données créées et dernière réponse reçue. */
public class ScenarioContext {

    private final Map<String, UUID> exploitations = new HashMap<>();
    private final Map<String, UUID> entrepots = new HashMap<>();
    private final Map<String, UUID> zones = new HashMap<>();
    private final Map<String, UUID> capteurs = new HashMap<>();
    private final Map<String, UUID> lots = new HashMap<>();

    private MvcResult reponse;
    private Exception erreur;

    public UUID exploitation(String nom) {
        return trouver(exploitations, nom, "exploitation");
    }

    public UUID entrepot(String nom) {
        return trouver(entrepots, nom, "entrepôt");
    }

    public UUID zone(String nom) {
        return trouver(zones, nom, "zone");
    }

    public UUID capteurDeLaZone(String nomZone) {
        return trouver(capteurs, nomZone, "capteur de la zone");
    }

    public UUID lot(String nom) {
        return trouver(lots, nom, "lot");
    }

    public void exploitation(String nom, UUID id) {
        exploitations.put(nom, id);
    }

    public void entrepot(String nom, UUID id) {
        entrepots.put(nom, id);
    }

    public void zone(String nom, UUID id) {
        zones.put(nom, id);
    }

    public void capteurDeLaZone(String nomZone, UUID id) {
        capteurs.put(nomZone, id);
    }

    public void lot(String nom, UUID id) {
        lots.put(nom, id);
    }

    public MvcResult reponse() {
        return reponse;
    }

    public void reponse(MvcResult reponse) {
        this.reponse = reponse;
        this.erreur = null;
    }

    public Exception erreur() {
        return erreur;
    }

    public void erreur(Exception erreur) {
        this.erreur = erreur;
        this.reponse = null;
    }

    private static UUID trouver(Map<String, UUID> donnees, String nom, String type) {
        UUID id = donnees.get(nom);
        if (id == null) {
            throw new IllegalStateException("Aucune donnée « " + type + " » nommée « " + nom + " » dans ce scénario");
        }
        return id;
    }
}
