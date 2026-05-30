# FutureKawa Backend - MSPR2

Backend IoT Spring Boot pour la supervision d'un entrepot de cafe.

Le service expose des APIs REST pour:
- gerer les lots,
- gerer les mesures capteurs (temperature/humidite),
- produire des alertes metier,
- lier automatiquement alertes <-> lots via la table `genere_alerte`.

Le projet suit une architecture hexagonale (ports/adapters) et integre MQTT pour l'ingestion et la simulation de donnees capteurs.

## 1) Objectif metier.

Ce backend represente un entrepot "owner" (backend deploye par entrepot). Il permet:
- d'enregistrer des mesures de capteurs rattaches a cet entrepot,
- de comparer les mesures aux seuils pays (`temp_ideale`, `humi_ideale`, tolerances),
- de creer des alertes quand les mesures sont hors plage,
- de rattacher ces alertes aux lots sur le meme emplacement,
- d'interroger les donnees via API REST.

## 2) Stack technique

- Java 21
- Spring Boot 3.2.3
- Spring Web
- Spring Data JPA / Hibernate
- PostgreSQL
- Spring Integration MQTT + Eclipse Paho
- Lombok
- springdoc-openapi
- Maven
- Docker / Docker Compose

Fichiers clefs:
- `pom.xml`
- `docker-compose.yml`
- `postgres/init.sql`
- `src/main/resources/application.yml`

## 3) Architecture du projet

### Couches

- `src/main/java/fr/epsi/mspr2/api/controller`
  - endpoints REST (`LotControlleur`, `MesureController`, `CapteurController`, `EntrepotController`)
- `src/main/java/fr/epsi/mspr2/domain/services`
  - logique applicative (`LotService`, `MesureService`, `CapteurService`, `AlerteService`)
- `src/main/java/fr/epsi/mspr2/domain/ports`
  - contrats metier (`LotPort`, `MesurePort`, `CapteurPort`, `AlertePort`)
- `src/main/java/fr/epsi/mspr2/infrastructure/adapters`
  - implementations techniques des ports (JPA, MQTT)
- `src/main/java/fr/epsi/mspr2/infrastructure/jpa`
  - repositories Spring Data
- `src/main/java/fr/epsi/mspr2/infrastructure/dto`
  - DTO request/response API
- `src/main/java/fr/epsi/mspr2/infrastructure/mapper`
  - mapping Domain <-> DTO
- `src/main/java/fr/epsi/mspr2/domain/models`
  - entites JPA

### Flux principal mesure -> alerte

1. Une mesure arrive via REST (`POST /api/mesures`) ou MQTT (`mqttInputChannel`).
2. `MesureService` persiste la mesure.
3. `AlerteService` delegue a `AlertePort` (`AlerteAdapter`).
4. `AlerteAdapter` charge `Capteur -> Entrepot -> Pays` puis evalue la compatibilite.
5. Si hors plage, une `Alerte` est creee.
6. Les lots du meme entrepot et du meme emplacement (`lot.emplacement == capteur.emplacement_precis`) sont lies via `GenereAlerte`.

## 4) Modele de donnees (etat actuel)

Le schema SQL est defini dans `postgres/init.sql`.

Tables principales:
- `pays`, `exploitation`, `entrepot`
- `capteur`, `mesure`
- `lot`, `alerte`, `genere_alerte`

Points importants:
- IDs en UUID (DB + JPA)
- cle composite `genere_alerte(id_lot, id_alerte)` mappee via `@EmbeddedId` (`GenereAlerteId`)
- seed automatique d'environ 30 capteurs (`A` a `AD`) sur l'entrepot:
  - `770e8400-e29b-41d4-a716-446655440011`

## 5) Configuration et variables d'environnement

### Proprietes application

Dans `src/main/resources/application.yml`:
- `spring.datasource.*` (PostgreSQL local)
- `app.entrepot.id` (UUID entrepot owner)
- `mqtt.broker-uri`
- `mqtt.topic`
- `mqtt.outbound-default-topic`

Les proprietes MQTT sont deja branchees sur des variables d'environnement:
- `MQTT_BROKER_URI` (default: `tcp://localhost:1883`)
- `MQTT_TOPIC` (default: `futurekawa/bresil/entrepot1/data`)
- `MQTT_OUTBOUND_DEFAULT_TOPIC` (default: `futurekawa/default`)

`app.entrepot.id` peut aussi etre override via environnement Spring standard:
- `APP_ENTREPOT_ID`

## 6) Lancement local

Prerequis:
- Java 21
- Maven 3.9+
- Docker Desktop (ou moteur Docker)

### 6.1 Demarrer les dependances

```powershell
docker compose up -d
```

Services exposes:
- PostgreSQL: `localhost:5433`
- Mosquitto MQTT: `localhost:1883` (WebSocket `9001`)
- Adminer: `http://localhost:8081`

### 6.2 Lancer le backend

```powershell
mvn spring-boot:run
```

ou avec profil local:

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Swagger:
- `http://localhost:8080/swagger-ui/index.html`

## 7) Endpoints REST disponibles

### Lots (`/api/lots`)

- `POST /api/lots`
  - cree un lot
  - response: `LotStatusResponseDTO { idLot, status }`
- `GET /api/lots/getLot/{id}`
  - recupere un lot par UUID
- `PUT /api/lots/lot/{id}`
  - met a jour l'emplacement d'un lot (via la zone) et cree un historique de transfert
  - response: `LotStatusResponseDTO { idLot, status }`
- `DELETE /api/lots/deleteLot/{id}`
  - supprime un lot
  - response: `LotStatusResponseDTO { idLot, status }`
- `GET /api/lots/allLotsFromAllEntrepots`
  - liste tous les lots de tous les entrepots
- `GET /api/lots/getAllQuantity`
  - inventaire mondial (quantite totale)

Exemple `POST /api/lots`:

```json
{
  "status": "CONFORME",
  "quantite": 120,
  "caracteristique": 2,
  "idZone": "cd2ba9e5-e5df-4970-89ff-3bfa84004c4f",
  "idExploitation": "aa0e8400-e29b-41d4-a716-446655440022"
}
```

Exemple `PUT /api/lots/lot/{id}`:

```json
{
  "idZone": "cd2ba9e5-e5df-4970-89ff-3bfa84004c4f"
}
```

### Mesures (`/api/mesures`)

- `POST /api/mesures`
  - cree une mesure
  - response: `MesureStatusResponseDTO { idMesure, status }`
- `GET /api/mesures/getMesure/{id}`
  - recupere une mesure par UUID
- `DELETE /api/mesures/deleteMesure/{id}`
  - supprime une mesure
  - response: `MesureStatusResponseDTO { idMesure, status }`

Exemple `POST /api/mesures`:

```json
{
  "temperature": 29.38,
  "humidite": 54.18,
  "dateMesure": "2026-03-15T15:42:44",
  "idCapteur": "b793e242-053a-4def-9cd8-96c990a586c9"
}
```

### Capteurs (`/api/capteurs`)

- `GET /api/capteurs/{capteurId}/mesures`
- `GET /api/capteurs/{capteurId}/mesures/by-day?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`
- `GET /api/capteurs/{capteurId}/mesures/by-hour?startTime=HH:mm:ss&endTime=HH:mm:ss`

### Entrepots (`/api/entrepots`)

- `GET /api/entrepots`

## 8) MQTT (ingestion + simulation)

Composants:
- `MqttConfig`: setup des channels et adapters inbound/outbound
- `MQTTAdapter`: lit les messages MQTT et appelle `MesureService`
- `SensorSimulator`: publie periodiquement des mesures simulees
- `MqttGateway`: envoi MQTT sortant

Details:
- topic inbound: `mqtt.topic`
- topic outbound par defaut: `mqtt.outbound-default-topic`
- simulation planifiee toutes les 30 secondes (`@Scheduled(fixedRate = 30000)`)
- la simulation utilise le premier entrepot et le premier capteur trouves en base

Format JSON attendu pour MQTT (meme structure que `MesureRequestDTO`):

```json
{
  "temperature": 27.50,
  "humidite": 56.20,
  "dateMesure": "2026-03-15T16:00:00",
  "idCapteur": "b793e242-053a-4def-9cd8-96c990a586c9"
}
```

## 9) Gestion des erreurs (etat actuel)

- `LotControlleur` et `MesureController` renvoient des DTO de statut pour create/delete.
- `CapteurController` gere les erreurs via `try/catch` local et renvoie `400/404` sans body.
- certains endpoints `getById` renvoient `null` si absence (a harmoniser eventuellement).

## 10) Tests

Tests presents dans `src/test/java`:
- unitaires (controllers: lot, mesure, capteur ; services: lot, mesure, capteur ; adapter: alerte ; mapper: mesure)
- BDD / Gherkin (Cucumber): tests d'integration pour la creation et le transfert de lots via API (`LotCreationSteps`, `LotUpdateSteps`)

Lancer tous les tests (unitaires et BDD):

```powershell
mvn test
```

Lancer une classe cible (ex. Cucumber):

```powershell
mvn test "-Dtest=fr.epsi.mspr2.gherkin.CucumberTest"
```

## 11) Points de vigilance pour contributors

- respecter la separation hexagonale (`domain` sans dependance technique)
- conserver les UUID partout (API/JPA/SQL)
- verifier la coherence `app.entrepot.id` <-> donnees seed SQL
- ne pas casser les contrats des DTO de statut (`id` + `status`)
- valider les impacts MQTT si changement de format JSON

## 12) Evolutions recommandees

- harmoniser completement la gestion d'erreurs via `@RestControllerAdvice`
- completer `RestSiegeAdapter`
- ajouter des tests d'integration (API + DB + MQTT)
- supprimer ou implementer les fichiers vides restants (`ApiErrorResponseDTO`, `MqttMessageListenerTest`)


