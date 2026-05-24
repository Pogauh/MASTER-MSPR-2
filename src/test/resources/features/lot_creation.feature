# language: fr
Fonctionnalité: Creation de lot
  Scénario: Creation d'un lot valide
    Étant donné un lot a creer avec statut "stocke", quantite 40, caracteristique 1, id exploitation "aa0e8400-e29b-41d4-a716-446655440022" et id zone "cd2ba9e5-e5df-4970-89ff-3bfa84004c4f"
    Quand je cree le lot via l'API
    Alors la reponse est 201 et un id de lot est retourne
    Et le lot est persiste avec l'exploitation et la zone

  Scénario: Creation d'un lot sans exploitation
    Étant donné un lot a creer sans exploitation avec statut "stocke", quantite 40, caracteristique 1 et id zone "cd2ba9e5-e5df-4970-89ff-3bfa84004c4f"
    Quand je cree le lot via l'API
    Alors une erreur est levee avec le message "L'id de l'exploitation est obligatoire pour enregistrer un lot"
    Et le lot n'est pas persiste
