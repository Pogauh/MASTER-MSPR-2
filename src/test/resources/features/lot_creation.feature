# language: fr
Fonctionnalité: Création de lot

  Contexte:
    Étant donné une exploitation "Flamenco"
    Et un entrepôt "Principal" avec une zone "A"

  Scénario: Création d'un lot valide
    Étant donné un lot à créer avec statut "COMPLIANT", quantité 40, caractéristique 1 dans la zone "A"
    Quand je crée le lot via l'API
    Alors la réponse est 201 et un id de lot est retourné
    Et le lot est enregistré avec l'exploitation et la zone

  Scénario: Création d'un lot sans exploitation
    Étant donné un lot à créer sans exploitation avec statut "COMPLIANT", quantité 40, caractéristique 1 dans la zone "A"
    Quand je crée le lot via l'API
    Alors une erreur est levée avec le message "L'id de l'exploitation est obligatoire pour enregistrer un lot"
    Et le lot n'est pas enregistré
