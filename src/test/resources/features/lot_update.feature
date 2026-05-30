# language: fr
Fonctionnalité: Mise a jour de l'emplacement d'un lot
  Scénario: Mise a jour reussie de l'emplacement d'un lot
    Étant donné un lot existant avec l'id "f10e4567-e89b-12d3-a456-426614174000"
    Et des nouvelles donnees d'emplacement avec id exploitation "aa0e8400-e29b-41d4-a716-446655440022" et id zone "cd2ba9e5-e5df-4970-89ff-3bfa84004c4f"
    Quand je mets a jour l'emplacement du lot via l'API
    Alors la reponse de mise a jour est 200
    Et l'historique de transfert est cree
    Et l'emplacement du lot est mis a jour

  Scénario: Mise a jour d'un lot inexistant
    Étant donné un lot inexistant avec l'id "f10e4567-e89b-12d3-a456-426614174000"
    Et des nouvelles donnees d'emplacement avec id exploitation "aa0e8400-e29b-41d4-a716-446655440022" et id zone "cd2ba9e5-e5df-4970-89ff-3bfa84004c4f"
    Quand je mets a jour l'emplacement du lot via l'API
    Alors la reponse de mise a jour est 404

