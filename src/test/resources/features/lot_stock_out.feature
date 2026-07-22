# language: fr
Fonctionnalité: Stock out d'un lot

  Scénario: Stock out d'un lot existant
    Étant donné un lot pour stock-out avec l'id "f10e4567-e89b-12d3-a456-426614174000"
    Quand je fais le stock-out du lot via l'API
    Alors la reponse de stock-out est 201 et un id d'historique est retourne
    Et le lot est marque comme expedie

  Scénario: Stock out avec statut lot mis a jour
    Étant donné un lot pour stock-out avec l'id "f10e4567-e89b-12d3-a456-426614174001"
    Quand je fais le stock-out du lot via l'API
    Alors la reponse de stock-out est 201 et un id d'historique est retourne

