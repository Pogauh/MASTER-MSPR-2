# FutureKawa Backend (pays) - MSPR2

Backend Spring Boot d'un **pays producteur** (Brésil, Colombie, Équateur…) de la chaîne FutureKawa : il supervise
les entrepôts de café de ce pays, reçoit les mesures des capteurs IoT (température / humidité), lève des alertes et
suit la vie des lots.

Une instance est déployée par pays. Le **siège** (`backend-siege`) agrège les instances pays : il appelle leurs API
REST (`/api/...`) telles que décrites plus bas, et le **front** passe par le siège.

```
ESP32 (module IoT) ──MQTT──▶ Mosquitto ──▶ backend pays ◀──REST── backend siège ◀── front
                                              │
                                          PostgreSQL
```

## 1) Règles métier

| Sujet | Règle |
|---|---|
| Structure | Un pays a des **entrepôts** ; un entrepôt a des **zones** ; chaque zone est surveillée par **un capteur** et contient des **lots**. |
| Mesure | Un capteur envoie `temperature`, `humidite`, `dateMesure` (REST ou MQTT). La date du jour est utilisée si elle est absente. |
| Plage idéale | Température et humidité idéales ± tolérance, configurées par pays (par défaut 29 ± 3 °C et 55 ± 2 %). |
| Alerte | Quand **5 mesures consécutives** d'un capteur sont hors plage : une alerte est créée (`HAUTETEMPERATURE`, `BASSETEMPERATURE`, `HAUTEHUMIDITE` ou `BASSEHUMIDITE`, la température étant examinée avant l'humidité), **tous les lots de la zone** passent au statut `ALERT` avec une trace dans leur historique, et le responsable est prévenu par e-mail. |
| Expiration | Un lot produit il y a **plus d'un an** passe au statut `EXPIRED` (contrôle chaque nuit à 1 h) : alerte `EXPIRED` + e-mail + trace dans l'historique. |
| FIFO | Le prochain lot à expédier est le **lot `COMPLIANT` stocké depuis le plus longtemps** (`GET /api/lots/oldestLot`). Sont à écouler en priorité (rapport de stock) les lots stockés depuis 9 à 12 mois. |
| Cycle de vie d'un lot | Création (statut `COMPLIANT` par défaut, date de stockage = date du jour) → transferts entre zones → `ALERT` / `BLOCKED` / `EXPIRED` → sortie de stock `SHIPPED`. Chaque transfert, alerte et expédition est tracé dans l'historique (`TRANSFERT`, `ALERTE`, `SHIPPED`). |

## 2) Stack technique

Java 21 · Spring Boot 3.2.3 (Web, Data JPA, Integration MQTT, Mail) · PostgreSQL 15 · Eclipse Paho · MapStruct ·
Lombok · springdoc-openapi · Maven · Docker / Docker Compose.
Tests : JUnit 5, Mockito, AssertJ, Cucumber, H2, ArchUnit, Testcontainers (PostgreSQL, Mosquitto).

## 3) Architecture hexagonale

```
 siège / front ──REST──▶ [ api ] ───▶ [ domain ] ◀─── [ infrastructure ] ───▶ PostgreSQL · Mosquitto · SMTP
                          contrôleurs    services        adaptateurs JPA et MQTT
                          DTO, mappers   ports           e-mail, planification
                                         modèles
                                         (sans framework)
```

`api` et `infrastructure` dépendent du domaine ; ils ne se connaissent pas et le domaine ne les connaît pas.

### Arborescence

```
src/main/java/fr/epsi/mspr2
├── Main.java
├── api/                          ← adaptateur entrant REST
│   ├── controller/               contrôleurs (…Controller) : reçoivent des DTO, appellent un service, renvoient des DTO
│   ├── dto/                      DTO de requête / réponse = contrat de l'API
│   ├── mapper/                   MapStruct : DTO <-> modèles du domaine
│   └── exception/                traduction des erreurs métier en réponses HTTP
├── domain/                       ← le cœur : ne dépend de rien (ni Spring, ni JPA, ni Jackson)
│   ├── models/                   Lot, Zone, Mesure, Alerte, Historique… + règles (SeuilsClimatiques, Lot.estExpire…)
│   ├── ports/                    interfaces vers l'extérieur : LotPort, MesurePort, AlertePort, NotificationPort, TransactionPort…
│   ├── services/                 cas d'usage : LotService, MesureService, AlerteService, ExpirationService, ReportingService…
│   └── exceptions/               erreurs métier (TransfertLotInvalideException, LotIntrouvableException)
└── infrastructure/               ← adaptateurs sortants et technique
    ├── config/                   DomainConfig (assemblage des services), propriétés de seuils d'alerte
    ├── persistence/              entity/ (entités JPA) · repository/ (Spring Data) · mapper/ (entité <-> domaine) · adapter/ (réalisent les ports)
    ├── messaging/mqtt/           MqttConfig, MqttMesureListener (MQTT entrant), MqttGateway (MQTT sortant), SensorSimulator
    ├── notification/             MailNotificationAdapter (réalise NotificationPort)
    └── scheduling/               LotExpirationScheduler (déclenche ExpirationService chaque nuit)
```

### Règles à respecter (vérifiées par `ArchitectureTest`)

- Le **domaine** n'importe ni `org.springframework`, ni `jakarta.persistence`, ni Jackson, ni `api`, ni `infrastructure`.
  Ses services sont de simples classes Java, instanciées par `infrastructure/config/DomainConfig`.
- **`api`** et **`infrastructure`** dépendent du domaine mais jamais l'un de l'autre ni du sens inverse.
- Les **entités JPA** n'existent que dans `infrastructure/persistence/entity` : le domaine manipule ses propres modèles,
  les adaptateurs font la conversion.
- Un contrôleur ne connaît que des **services** et des **mappers** (jamais un port ou un repository).
- Les **transactions** sont portées par le domaine à travers `TransactionPort` (réalisé par `TransactionAdapter`) :
  un traitement à plusieurs écritures (alerte + lots + historique, transfert, sortie de stock…) est atomique.

### Flux principaux

**Mesure → alerte**
1. La mesure arrive par `POST /api/mesures` ou par MQTT (`MqttMesureListener`) → `MesureService.createMesure`.
2. La mesure est enregistrée (elle reste enregistrée même si l'étape suivante échoue).
3. `AlerteService.evaluerMesure` compare la mesure et les 4 précédentes du capteur à la plage idéale (`SeuilsClimatiques`).
4. Si les 5 sont hors plage : **dans une seule transaction**, l'alerte est enregistrée, les lots de la zone passent en `ALERT` et
   leur historique reçoit une trace ; **puis** (après validation de la transaction) le mail est envoyé.

**Expiration** : `LotExpirationScheduler` (cron `0 0 1 * * ?`) → `ExpirationService.expirerLotsPerimes` → pour chaque lot périmé,
dans une transaction : alerte `EXPIRED`, statut `EXPIRED`, historique ; puis e-mail.

**Transfert d'un lot** : `PUT /api/lots/{id}/zone` → `LotService.updateEmplacementLot` : refus (400) si la destination est absente ou
identique à la zone actuelle ; sinon historique `TRANSFERT` + nouvelle zone, ensemble.

## 4) Modèle de données

Le schéma est créé par `postgres/init.sql` (démarrage du conteneur) puis complété par Hibernate (`ddl-auto: update`).
Tables : `entrepot`, `zone`, `capteur`, `mesure`, `exploitation`, `lot`, `historique`, `alerte`. Identifiants en UUID.

- `zone` appartient à un `entrepot` (nom unique par entrepôt) ; `capteur` est rattaché à une seule `zone`.
- `lot` appartient à une `zone` et à une `exploitation` ; `historique` appartient à un `lot`.
- `alerte` référence le `capteur` qui l'a déclenchée (vide pour une alerte d'expiration).
- Jeu de données initial : 2 entrepôts, des zones `A`…`AD` (+ `AF`) avec un capteur chacune, une exploitation et des lots aléatoires.
  Entrepôt principal : `770e8400-e29b-41d4-a716-446655440011`.

## 5) Configuration

Tout se règle par variables d'environnement (valeurs par défaut dans `src/main/resources/application.yml`).

| Variable | Défaut | Rôle |
|---|---|---|
| `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USER` / `DB_PASSWORD` | `localhost` / `5433` / `db_bresil` / `admin_bresil` / `password_bresil` | PostgreSQL |
| `API_NAME` | `bresil` | Nom du pays (plages du simulateur : `bresil`, `equateur`, `colombie`) |
| `TEMPERATURE_IDEALE` / `TOLERANCE_TEMPERATURE` | `29` / `3` | Plage de température (°C) |
| `HUMIDITE_IDEALE` / `TOLERANCE_HUMIDITE` | `55` / `2` | Plage d'humidité (%) |
| `MQTT_ENABLED` | `true` | `false` : aucune connexion au broker (ni écoute, ni envoi) |
| `MQTT_BROKER_URI` | `tcp://localhost:1883` | Broker MQTT |
| `MQTT_TOPIC` | `futurekawa/bresil/entrepot1/data` | Topic des mesures |
| `MQTT_OUTBOUND_DEFAULT_TOPIC` | `futurekawa/default` | Topic d'envoi par défaut |
| `SIMULATOR_ENABLED` | `true` | `false` : désactive le simulateur de capteurs |
| `MAIL_HOST` / `MAIL_PORT` / `MAIL_USERNAME` / `MAIL_PASSWORD` | voir `application.yml` | Serveur SMTP |
| `MAIL_RESPONSABLE` / `MAIL_FROM` | `ultrabaga@hotmail.com` | Destinataire / expéditeur des mails d'alerte |

> ⚠️ Les valeurs par défaut `MAIL_USERNAME` / `MAIL_PASSWORD` de `application.yml` sont des identifiants SMTP committés :
> à remplacer par des variables d'environnement (et à révoquer côté fournisseur).

Les propriétés `app.alert.tolerences.*` sont lues par `SeuilsAlerteProperties` ; le démarrage échoue si l'une d'elles manque.

## 6) Lancement local

Prérequis : Java 21, Maven 3.9+, Docker.

```powershell
docker compose up -d          # PostgreSQL (5433), Mosquitto (1883, WebSocket 9001), Adminer (http://localhost:8081)
mvn spring-boot:run           # ou : mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

Swagger : `http://localhost:8080/swagger-ui/index.html`.
Attention : le simulateur est actif par défaut et publie une mesure par capteur chaque minute ; utiliser `SIMULATOR_ENABLED=false`
pour ne pas polluer les données.

## 7) API REST

Les réponses ci-dessous sont le **contrat** consommé par le siège et le front : ne pas les modifier sans concertation
(`ApiContractIT` les verrouille).

### Lots — `/api/lots`

| Méthode et chemin | Description | Réponse |
|---|---|---|
| `POST /api/lots` | Crée un lot (exploitation et zone obligatoires) | `201` `{ idLot, status: "CREATED" }` |
| `GET /api/lots/getLot/{idLot}` | Un lot avec son historique | `200` `LotResponseDTO` / `404` sans corps |
| `GET /api/lots/allLotsFromAllEntrepots` | Tous les lots, du plus anciennement stocké au plus récent | `200` `LotResponseDTO[]` |
| `GET /api/lots/getAllQuantity` | Quantité totale en stock | `200` entier |
| `GET /api/lots/oldestLot` | Lot à expédier en premier (FIFO) | `200` `LotResponseDTO` / `404` |
| `GET /api/lots/getAllMesuresByLotId/{idLot}` | Mesures des zones traversées pendant le séjour du lot | `200` `[{ temperature, humidite, dateMesure }]` / `404` si aucune |
| `PUT /api/lots/{idLot}/zone` | Transfère le lot : `{ "idZone": "…" }` | `200` `{ idLot, status: "OK" }` / `400` (même zone ou zone absente) / `404` `{ idLot: null, status: "NOT_FOUND" }` |
| `PUT /api/lots/{idLot}/status` | Change le statut : `{ "status": "BLOCKED" }` (sans statut : lot inchangé) | idem |
| `DELETE /api/lots/deleteLot/{idLot}` | Supprime un lot | `200` `{ idLot, status: "OK" }` / `404` `{ idLot: null, status: "NOT_FOUND" }` |
| `POST /api/lots/stock-out?lotId=…` | Expédie un lot (statut `SHIPPED`) | `201` `{ idHistorique, status: "CREATED" }` |

`POST /api/lots` :

```json
{ "status": "COMPLIANT", "quantite": 120, "caracteristique": 2, "dateProduction": "2026-03-15",
  "idExploitation": "aa0e8400-e29b-41d4-a716-446655440022", "idZone": "<uuid>" }
```

`LotResponseDTO` : `{ idLot, status, quantite, caracteristique, dateSortie, dateStockage, dateProduction, emplacement (idZone), idExploitation, idEntrepot, historique: [{ idHistorique, type, dateEmission, zonePrecedente, zoneActuelle, detail }] }`.

### Historique — `/api/historique`

`GET /api/historique/allHistoriquesForLot/{idLot}?type=TRANSFERT|ALERTE|SHIPPED` — le filtre `type` est facultatif,
insensible à la casse, `ALERT` est accepté comme alias de `ALERTE` ; un type inconnu renvoie `400` sans corps.

### Mesures, capteurs, zones

| Méthode et chemin | Description |
|---|---|
| `POST /api/mesures` | Crée une mesure `{ temperature, humidite, dateMesure, idCapteur }` → `201` `{ idMesure: null, status: "CREATED" }`¹ |
| `GET /api/mesures/getMesure/{id}` | Une mesure (`200` avec corps vide si elle n'existe pas) |
| `DELETE /api/mesures/deleteMesure/{id}` | Supprime une mesure → `200` `{ idMesure, status: "OK" }` / `404` `{ idMesure: null, status: "NOT_FOUND" }` |
| `GET /api/capteurs/{capteurId}/mesures` | Toutes les mesures d'un capteur |
| `GET /api/zone/readings/{zoneId}?startDate=…&endDate=…` | Mesures d'une zone sur une période (`2026-03-15T10:00`), `400` si la période est inversée |
| `GET /api/zone/{zoneId}/readings/latest` | Dernière mesure d'une zone (`404` si aucune) |

¹ Comportement historique conservé : l'identifiant de la mesure créée n'est pas renvoyé.

### Entrepôts, exploitations, alertes, reporting

| Méthode et chemin | Description |
|---|---|
| `GET /api/entrepots` | `[{ idEntrepot, localisation, nom }]` |
| `GET /api/entrepots/{id}/zones` | `[{ idZone, nomZone, idEntrepot, idCapteur, lotCount }]` |
| `GET /api/entrepots/{id}/capteurSummary` | Par zone : dernière mesure et moyennes sur 24 h `{ zoneId, zoneName, lastTemp, lastHumidity, avgTemp24h, avgHumidity24h }` |
| `GET /api/exploitation/allExploitations` | `[{ idExploitation, nom, adresse }]` |
| `GET /api/alertes?idEntrepot=…&startDate=…&endDate=…` | Alertes des capteurs de l'entrepôt `[{ idAlerte, typeAlerte, description, dateEmission, idZone }]` (dates facultatives, `idEntrepot` obligatoire) |
| `GET /api/reporting/global` | `{ totalLots, avgAgeDays, complianceRate, countMouvementLast30Days }` |
| `GET /api/reporting/quality?startDate=…&endDate=…` | `{ alertCount, complianceRate, nonComplianceCount }` |
| `GET /api/reporting/stock?startDate=…&endDate=…` | `{ totalLots, avgAgeDays, complianceRate, lotIdsUrgentFifo }` (les dates sont exigées mais le rapport porte sur le stock courant) |

Sans aucun lot, les rapports renvoient des zéros.

### Gestion des erreurs

- Erreurs de validation du domaine (`IllegalArgumentException` : exploitation ou zone manquante, mesure sans capteur…) : `500` (non interceptées, comme avant).
- Transfert invalide : `400` avec le message dans le corps d'erreur standard de Spring Boot.
- Sortie de stock d'un lot inconnu : `500` (`LotIntrouvableException`).
- Contrôleurs capteur / zone / entrepôt / alertes : `IllegalArgumentException` → `400` sans corps, `IllegalStateException` → `404`.

## 8) MQTT

- `MqttConfig` : canaux et connexion au broker (désactivable avec `MQTT_ENABLED=false`).
- `MqttMesureListener` : lit les messages du topic `MQTT_TOPIC` et appelle `MesureService` ; un message illisible ou refusé est journalisé et ignoré.
- `MqttGateway` : envoi de messages sortants.
- `SensorSimulator` : toutes les 60 s, publie une mesure par capteur dans les plages du pays (`API_NAME`).

Format du message (identique à `POST /api/mesures`, c'est ce que publie l'ESP32 du module IoT) :

```json
{ "temperature": 27.50, "humidite": 56.20, "dateMesure": "2026-03-15T16:00:00", "idCapteur": "b793e242-053a-4def-9cd8-96c990a586c9" }
```

## 9) Tests

```powershell
mvn test       # tests unitaires et architecture (rapides, sans Docker)
mvn verify     # + tests d'intégration (*IT) : API complète sur H2, Cucumber, PostgreSQL et Mosquitto réels
```

| Type | Contenu |
|---|---|
| **Unitaires** (`*Test`) | Modèles et services du domaine (Mockito sur les ports), contrôleurs (`@WebMvcTest`, JSON exact), mappers MapStruct, écouteur MQTT, simulateur, notification e-mail, planificateur, transaction. |
| **Architecture** (`ArchitectureTest`) | Sens des dépendances, domaine sans framework, entités JPA confinées, contrôleurs et ports bien utilisés (ArchUnit). |
| **Contrat d'API** (`ApiContractIT`) | Boîte noire : chaque endpoint REST et le canal MQTT sur une base H2, réponses JSON comparées exactement. Doit rester vert quelle que soit l'organisation interne. |
| **Persistance** (`*AdapterIT`) | Repositories, mappers et adaptateurs sur H2 ; les mêmes scénarios sont rejoués sur **PostgreSQL 15** par `*PostgresIT`. |
| **Atomicité** (`AtomicityIT`) | Une panne en cours de traitement ne laisse aucune écriture partielle et n'envoie aucun mail. |
| **Assemblage** (`WiringIT`) | Services du domaine instanciés, tâche planifiée déclarée, simulateur / broker coupés en test. |
| **MQTT** (`MqttBrokerIT`) | Chaîne complète avec un vrai broker Mosquitto : capteur → broker → base, simulateur → broker → base, passerelle sortante. |
| **BDD** (`CucumberIT`) | Scénarios Gherkin en français (`src/test/resources/features`) : création et transfert de lot, alertes sur mesures, FIFO et sortie de stock, expiration. |

- Les tests `*PostgresIT` et `MqttBrokerIT` sont **ignorés automatiquement si Docker n'est pas disponible**.
- Les tests unitaires tournent avec Surefire, les `*IT` avec Failsafe (`mvn verify`).
- Couverture (facultatif) : `mvn org.jacoco:jacoco-maven-plugin:0.8.12:prepare-agent verify org.jacoco:jacoco-maven-plugin:0.8.12:report` puis `target/site/jacoco/index.html`.

CI GitHub Actions : `dev-ci.yml` et `master-ci.yml` exécutent `mvn clean verify` ; `master-ci.yml` publie ensuite l'image Docker.

## 10) Points de vigilance pour les contributeurs

- Respecter la séparation hexagonale : une nouvelle règle métier va dans `domain` (modèle ou service), une nouvelle dépendance
  technique derrière un **port** réalisé par un adaptateur de `infrastructure`.
- Ne pas casser les **DTO** de `api/dto` (noms de champs, statuts renvoyés) : le siège et le front les consomment.
- Garder les **UUID** partout (API, JPA, SQL) et un nommage de tables / colonnes inchangé.
- Toute modification du format JSON MQTT impacte le module IoT.
- Les listes renvoyées sans tri explicite (zones d'un entrepôt, alertes, historique) n'ont pas d'ordre garanti, comme avant la refactorisation.

## 11) Évolutions recommandées

- Gestion d'erreurs homogène via `@RestControllerAdvice` (aujourd'hui seules les erreurs de transfert sont traduites) — attention, cela
  change des codes HTTP consommés par le siège.
- `POST /api/mesures` : renvoyer l'`idMesure` créé (aujourd'hui `null`) ; `stock-out` d'un lot inconnu : renvoyer `404` au lieu de `500`.
- Une alerte place **tous** les lots de la zone en `ALERT`, y compris ceux déjà expédiés ou expirés : à restreindre aux lots en stock.
- Sortir les identifiants SMTP du dépôt ; passer `spring.jpa.open-in-view` à `false` (les entités ne sortent plus de la couche de persistance).
- Trier explicitement les listes renvoyées (`ORDER BY`).
