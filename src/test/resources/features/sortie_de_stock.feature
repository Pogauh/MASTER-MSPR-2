# language: fr
Fonctionnalité: Sortie de stock des lots
  Les lots sont expédiés selon la règle FIFO : le premier lot à sortir est le lot conforme
  stocké depuis le plus longtemps.

  Contexte:
    Étant donné une exploitation "Flamenco"
    Et un entrepôt "Principal" avec une zone "A"

  Scénario: Le lot à expédier en premier est le plus ancien lot conforme
    Étant donné un lot "Recent" conforme stocké depuis 10 jours dans la zone "A"
    Et un lot "Ancien" conforme stocké depuis 200 jours dans la zone "A"
    Et un lot "Bloque" au statut "BLOCKED" stocké depuis 400 jours dans la zone "A"
    Quand je demande le lot à expédier en premier
    Alors la réponse est 200
    Et le lot renvoyé est "Ancien"

  Scénario: Aucun lot conforme à expédier
    Étant donné un lot "Bloque" au statut "BLOCKED" stocké depuis 400 jours dans la zone "A"
    Quand je demande le lot à expédier en premier
    Alors la réponse est 404

  Scénario: Sortie de stock d'un lot
    Étant donné un lot "L1" conforme stocké depuis 10 jours dans la zone "A"
    Quand je sors le lot "L1" du stock
    Alors la réponse est 201
    Et le lot "L1" a le statut "SHIPPED"
    Et le lot "L1" a une date de sortie
    Et une expédition est enregistrée dans l'historique du lot "L1"

  Scénario: Sortie de stock d'un lot inexistant
    Quand je sors un lot inexistant du stock
    Alors une erreur est levée avec le message "Lot introuvable"
    Et aucune expédition n'est enregistrée
