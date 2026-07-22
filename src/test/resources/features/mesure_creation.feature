# language: fr
Fonctionnalité: Gestion des mesures

  Scénario: Creation d'une mesure valide
    Étant donné une mesure a creer avec temperature 22.5, humidite 60.0 et id capteur "aa0e8400-e29b-41d4-a716-446655440001"
    Quand je cree la mesure via l'API
    Alors la reponse de mesure est 201 et un id de mesure est retourne

  Scénario: Creation d'une mesure sans id capteur
    Étant donné une mesure a creer avec temperature 22.5, humidite 60.0 et sans id capteur
    Quand je cree la mesure via l'API
    Alors la reponse de mesure est une erreur serveur

  Scénario: Suppression d'une mesure existante
    Étant donné une mesure existante avec l'id "cc0e8400-e29b-41d4-a716-446655440033"
    Quand je supprime la mesure via l'API
    Alors la reponse de suppression de mesure est 200

  Scénario: Suppression d'une mesure inexistante
    Étant donné une mesure inexistante avec l'id "cc0e8400-e29b-41d4-a716-446655440099"
    Quand je supprime la mesure via l'API
    Alors la reponse de suppression de mesure est 404

