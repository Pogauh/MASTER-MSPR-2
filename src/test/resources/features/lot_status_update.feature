# language: fr
Fonctionnalité: Mise a jour du statut d'un lot

  Scénario: Mise a jour du statut reussie
    Étant donné un lot existant avec l'id "f10e4567-e89b-12d3-a456-426614174000"
    Et un nouveau statut "SHIPPED"
    Quand je mets a jour le statut du lot via l'API
    Alors la reponse de mise a jour de statut est 200

  Scénario: Mise a jour du statut d'un lot inexistant
    Étant donné un lot inexistant avec l'id "f10e4567-e89b-12d3-a456-426614174000"
    Et un nouveau statut "SHIPPED"
    Quand je mets a jour le statut du lot via l'API
    Alors la reponse de mise a jour de statut est 404

