# language: fr
Fonctionnalité: Expiration des lots
  Un lot stocké depuis plus d'un an est périmé : il passe au statut EXPIRED
  et une alerte est levée pour prévenir le responsable.
  La date de production du lot n'entre pas en compte : seule la durée de stockage compte.

  Contexte:
    Étant donné une exploitation "Flamenco"
    Et un entrepôt "Principal" avec une zone "A"

  Scénario: Un lot stocké depuis plus d'un an expire
    Étant donné un lot "Vieux" conforme stocké depuis 400 jours dans la zone "A"
    Et un lot "Recent" conforme stocké depuis 100 jours dans la zone "A"
    Quand la vérification des lots périmés s'exécute
    Alors le lot "Vieux" a le statut "EXPIRED"
    Et le lot "Recent" a le statut "COMPLIANT"
    Et une alerte d'expiration est enregistrée pour le lot "Vieux"
    Et un e-mail d'alerte est envoyé au responsable

  Scénario: La date de production n'entre pas en compte
    Étant donné un lot "Ancienne production" produit il y a 800 jours et stocké depuis 10 jours dans la zone "A"
    Et un lot "Ancien stock" produit il y a 5 jours et stocké depuis 400 jours dans la zone "A"
    Quand la vérification des lots périmés s'exécute
    Alors le lot "Ancienne production" a le statut "COMPLIANT"
    Et le lot "Ancien stock" a le statut "EXPIRED"
    Et une alerte d'expiration est enregistrée pour le lot "Ancien stock"

  Scénario: Aucun lot périmé
    Étant donné un lot "Recent" conforme stocké depuis 100 jours dans la zone "A"
    Quand la vérification des lots périmés s'exécute
    Alors le lot "Recent" a le statut "COMPLIANT"
    Et aucune alerte n'est enregistrée
    Et aucun e-mail d'alerte n'est envoyé
