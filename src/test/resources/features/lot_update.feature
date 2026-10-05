# language: fr
Fonctionnalité: Mise à jour de l'emplacement d'un lot

  Contexte:
    Étant donné une exploitation "Flamenco"
    Et un entrepôt "Principal" avec une zone "A"
    Et la zone "B" dans l'entrepôt "Principal"
    Et un lot "L1" conforme de 100 kg stocké dans la zone "A"

  Scénario: Mise à jour réussie de l'emplacement d'un lot
    Quand je déplace le lot "L1" vers la zone "B"
    Alors la réponse est 200
    Et le lot "L1" est dans la zone "B"
    Et un transfert de la zone "A" vers la zone "B" est enregistré dans l'historique du lot "L1"

  Scénario: Transfert vers la zone où se trouve déjà le lot
    Quand je déplace le lot "L1" vers la zone "A"
    Alors la réponse est 400
    Et le message d'erreur est "Erreur de transfert : l'emplacement de destination est identique à l'emplacement actuel."
    Et aucun transfert n'est enregistré

  Scénario: Mise à jour d'un lot inexistant
    Quand je déplace un lot inexistant vers la zone "B"
    Alors la réponse est 404
