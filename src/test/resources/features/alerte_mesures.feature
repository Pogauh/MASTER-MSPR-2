# language: fr
Fonctionnalité: Alertes sur les mesures hors tolérance
  La température idéale est de 29 °C (± 3) et l'humidité idéale de 55 % (± 2).
  Une alerte est levée quand cinq mesures consécutives d'un capteur sont hors tolérance :
  les lots de sa zone passent en alerte et le responsable est prévenu par e-mail.

  Contexte:
    Étant donné une exploitation "Flamenco"
    Et un entrepôt "Principal" avec une zone "A"
    Et la zone "B" dans l'entrepôt "Principal"
    Et un capteur surveille la zone "A"
    Et un lot "L1" conforme de 100 kg stocké dans la zone "A"
    Et un lot "L2" conforme de 100 kg stocké dans la zone "B"

  Scénario: Cinq mesures trop chaudes déclenchent une alerte
    Quand le capteur de la zone "A" envoie 5 mesures de 35 degrés et 55 % d'humidité
    Alors une alerte "HAUTETEMPERATURE" est enregistrée pour la zone "A"
    Et la description de l'alerte est "Temperature trop haute: 35 (ideal=29, tolerance=3)"
    Et le lot "L1" a le statut "ALERT"
    Et l'historique du lot "L1" mentionne l'alerte
    Et le lot "L2" a le statut "COMPLIANT"
    Et un e-mail d'alerte est envoyé au responsable

  Scénario: Cinq mesures trop sèches déclenchent une alerte d'humidité
    Quand le capteur de la zone "A" envoie 5 mesures de 29 degrés et 40 % d'humidité
    Alors une alerte "BASSEHUMIDITE" est enregistrée pour la zone "A"
    Et la description de l'alerte est "Humidite trop basse: 40 (ideal=55, tolerance=2)"
    Et le lot "L1" a le statut "ALERT"

  Scénario: Quatre mesures hors tolérance ne suffisent pas
    Quand le capteur de la zone "A" envoie 4 mesures de 35 degrés et 55 % d'humidité
    Alors aucune alerte n'est enregistrée
    Et le lot "L1" a le statut "COMPLIANT"
    Et aucun e-mail d'alerte n'est envoyé

  Scénario: Une mesure dans la tolérance interrompt la série
    Quand le capteur de la zone "A" envoie 3 mesures de 35 degrés et 55 % d'humidité
    Et le capteur de la zone "A" envoie une mesure de 29 degrés et 55 % d'humidité
    Et le capteur de la zone "A" envoie 3 mesures de 35 degrés et 55 % d'humidité
    Alors aucune alerte n'est enregistrée
    Et le lot "L1" a le statut "COMPLIANT"

  Scénario: Des mesures dans la tolérance ne déclenchent rien
    Quand le capteur de la zone "A" envoie 6 mesures de 29 degrés et 55 % d'humidité
    Alors aucune alerte n'est enregistrée
    Et aucun e-mail d'alerte n'est envoyé
