# language: fr
Fonctionnalité: Expiration des lots
  Un lot produit il y a plus d'un an est périmé : il passe au statut EXPIRED
  et une alerte est levée pour prévenir le responsable.

  Contexte:
    Étant donné une exploitation "Flamenco"
    Et un entrepôt "Principal" avec une zone "A"

  Scénario: Un lot produit il y a plus d'un an expire
    Étant donné un lot "Vieux" produit il y a 400 jours stocké dans la zone "A"
    Et un lot "Recent" produit il y a 100 jours stocké dans la zone "A"
    Quand la vérification des lots périmés s'exécute
    Alors le lot "Vieux" a le statut "EXPIRED"
    Et le lot "Recent" a le statut "COMPLIANT"
    Et une alerte d'expiration est enregistrée pour le lot "Vieux"
    Et un e-mail d'alerte est envoyé au responsable

  Scénario: Aucun lot périmé
    Étant donné un lot "Recent" produit il y a 100 jours stocké dans la zone "A"
    Quand la vérification des lots périmés s'exécute
    Alors le lot "Recent" a le statut "COMPLIANT"
    Et aucune alerte n'est enregistrée
    Et aucun e-mail d'alerte n'est envoyé
