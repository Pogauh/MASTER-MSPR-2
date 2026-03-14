# FutureKawa Backend - MSPR2

Backend Java/Spring Boot du projet FutureKawa.

Ce service expose des API REST pour gerer des **mesures** et des **lots**, avec une architecture orientee hexagonale (ports/adapters) pour separer la logique metier de l'infrastructure.

## 1) Objectif backend

L'objectif est de centraliser la gestion de donnees metier autour des entrepots (mesures entrepots, gestion des lots) et de fournir une base evolutive pour :

- exposition d'API REST,
- persistance PostgreSQL,
- integration MQTT (en cours),
- integration avec un service siege (en cours).

## 2) Stack technique

- **Java 21**
- **Spring Boot 3.2.3**
- **Spring Web** (API REST)
- **Spring Data JPA / Hibernate**
- **PostgreSQL**
- **Maven**
- **Lombok**
- **springdoc-openapi** (Swagger UI)
- **Docker Compose** (Postgres, Mosquitto, Adminer)

Fichiers de reference :
- `pom.xml`
- `docker-compose.yml`
- `src/main/resources/application.yml`

## 3) Architecture

Le projet suit une architecture hexagonale simplifiee :

- `domain/models` : entites et objets metier (`Lot`, `Mesure`, `Entrepot`, ...)
- `domain/ports` : interfaces metier pour acces aux donnees (`LotPort`, `MesurePort`)
- `domain/services` : logique applicative (`ServiceLot`, `ServiceMesure`)
- `adapters` : implementations techniques des ports (`AdapterLot`, `AdapterMesure`, repositories JPA)
- `api/controller` : points d'entree HTTP (`LotControlleur`, `MesureController`)
- `adapters/dto` + `adapters/mapper` : contrats API et transformations modele <-> DTO
- `config` : configuration applicative (`EntrepotOwnerProperties`)

### Flux de traitement (exemple REST)

1. Le controller recoit la requete HTTP.
2. Le controller appelle le service de domaine.
3. Le service utilise un port (`LotPort` / `MesurePort`).
4. L'adapter implemente ce port via Spring Data JPA.
5. La reponse est mappee en DTO puis renvoyee au front.

## 4) Organisation du code (resume)

- Point d'entree Spring Boot : `src/main/java/fr/epsi/mspr2/Main.java`
- API Lot : `src/main/java/fr/epsi/mspr2/api/controller/LotControlleur.java`
- API Mesure : `src/main/java/fr/epsi/mspr2/api/controller/MesureController.java`
- Service Lot : `src/main/java/fr/epsi/mspr2/domain/services/ServiceLot.java`
- Service Mesure : `src/main/java/fr/epsi/mspr2/domain/services/ServiceMesure.java`

## 5) Contrat API actuel (important)

Pour les routes de **creation** et **suppression** de `Lot` et `Mesure` :

- la reponse contient l'`id` de l'item concerne,
- et un champ `status` (valeur du statut HTTP, ex: `CREATED`, `OK`, `NOT_FOUND`).

Exemples de DTO :
- `LotStatusResponseDTO` (`idLot`, `status`)
- `MesureStatusResponseDTO` (`idMesure`, `status`)

## 6) Prerequis

- Java 21 installe
- Maven installe
- Docker + Docker Compose installes

## 7) Lancer le projet en local

### 7.1 Demarrer les dependances (Postgres, MQTT, Adminer)

```bash
docker compose up -d
```

Services exposes :
- Postgres : `localhost:5433`
- Mosquitto MQTT : `localhost:1883`
- Adminer : `http://localhost:8081`

### 7.2 Lancer l'application Spring Boot

```bash
mvn spring-boot:run
```

L'application lit sa config dans :
- `src/main/resources/application.yml`

Parametres DB locaux par defaut :
- URL : `jdbc:postgresql://localhost:5433/futurekawa_country`
- user : `admin`
- password : `password123`
- serveur : `db-sql`

## 8) Tests

Executer tous les tests unitaires :

```bash
mvn test
```

Exemples de tests presents :
- `src/test/java/fr/epsi/mspr2/api/controller/MesureControllerTest.java`
- `src/test/java/fr/epsi/mspr2/api/controller/LotControlleurTest.java`
- `src/test/java/fr/epsi/mspr2/domain/services/ServiceMesureTest.java`
- `src/test/java/fr/epsi/mspr2/domain/services/ServiceLotTest.java`

## 9) Documentation API

Avec `springdoc-openapi`, l'UI Swagger est generalement accessible sur :

- `http://localhost:8080/swagger-ui/index.html`

## 10) Conventions pour ceux qui veulent dev

- Respecter la separation **domain / ports / adapters / api**.
- Eviter de mettre de la logique metier dans les controllers.
- Ajouter/mettre a jour les tests unitaires controller + service a chaque evolution.
- Garder les DTO API stables (surtout sur create/delete).
- Privilegier l'injection par constructeur.

## 11) Pistes d'evolution

- Completer `AdapterMQTT` pour ingestion automatique des mesures.
- Completer `AdapterRestSiege` pour synchronisation avec le siege.
- Ajouter une gestion d'erreurs globale (`@ControllerAdvice`).
- Renforcer la securisation des secrets (variables d'environnement/profils Spring).

