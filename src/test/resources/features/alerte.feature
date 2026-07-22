# language: fr
Fonctionnalité: Consultation des alertes

  Scénario: Recuperation des alertes par entrepot avec dates
    Étant donné un entrepot avec l'id "bb0e8400-e29b-41d4-a716-446655440011"
    Quand je consulte les alertes de l'entrepot entre "2026-01-01T00:00:00" et "2026-12-31T23:59:59"
    Alors la reponse des alertes est 200 et la liste est retournee

  Scénario: Recuperation des alertes sans dates
    Étant donné un entrepot avec l'id "bb0e8400-e29b-41d4-a716-446655440011"
    Quand je consulte les alertes de l'entrepot sans dates
    Alors la reponse des alertes est 200 et la liste est retournee

