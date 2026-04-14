CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE exploitation (
    id_exploitation UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nom VARCHAR(50),
    adresse VARCHAR(70)
);

CREATE TABLE entrepot (
    id_entrepot UUID PRIMARY KEY,
    nom VARCHAR(50),
    localisation VARCHAR(150)
);

CREATE TABLE zone (
    id_zone UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nom VARCHAR(50) NOT NULL,
    id_entrepot UUID NOT NULL REFERENCES entrepot(id_entrepot) ON DELETE CASCADE,
    UNIQUE (nom, id_entrepot)
);

CREATE TABLE capteur (
    id_capteur UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code_serie VARCHAR(50) NOT NULL UNIQUE,
    id_zone UUID NOT NULL REFERENCES zone(id_zone) ON DELETE CASCADE,
    UNIQUE (id_zone)
);

CREATE TABLE lot (
    id_lot UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    status VARCHAR(20),
    quantite INT,
    caracteristique INT,
    date_sortie TIMESTAMP,
    date_stockage TIMESTAMP,
    date_production DATE,
    id_exploitation UUID NOT NULL REFERENCES exploitation(id_exploitation),
    id_zone UUID NOT NULL REFERENCES zone(id_zone)
);

CREATE TABLE mesure (
    id_mesure UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    temperature INT,
    humidite INT,
    date_mesure TIMESTAMP,
    id_capteur UUID NOT NULL REFERENCES capteur(id_capteur)
);

CREATE TABLE alerte (
    id_alerte UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    type_alerte VARCHAR(50),
    description VARCHAR(50),
    date_emission TIMESTAMP,
    id_capteur UUID NOT NULL REFERENCES capteur(id_capteur)
);

CREATE TABLE historique (
    id_historique UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    type VARCHAR(50),
    date_emission TIMESTAMP,
    zone_precedente VARCHAR(50),
    id_lot UUID NOT NULL REFERENCES lot(id_lot)
);

INSERT INTO entrepot (id_entrepot, nom, localisation)
VALUES ('770e8400-e29b-41d4-a716-446655440011', 'Entrepot Principal', 'Bresil')
VALUES ('3f7c9e8a-6b2d-4d91-9a5e-2c8f1b7e4a63', 'Entrepot Secondaire', 'Bresil')

ON CONFLICT (id_entrepot) DO NOTHING;

WITH zone_seed (nom_zone) AS (
    VALUES
        ('A'), ('B'), ('C'), ('D'), ('E'), ('F'), ('G'), ('H'),
        ('I'), ('J'), ('K'), ('L'), ('M'), ('N'), ('O'), ('P'),
        ('Q'), ('R'), ('S'), ('T'), ('U'), ('V'), ('W'), ('X'),
        ('Y'), ('Z'), ('AA'), ('AB'), ('AC'), ('AD')
)
INSERT INTO zone (id_zone, nom, id_entrepot)
SELECT
    uuid_generate_v4(),
    zone_seed.nom_zone,
    '770e8400-e29b-41d4-a716-446655440011'
FROM zone_seed
ON CONFLICT (nom, id_entrepot) DO NOTHING;

WITH capteur_seed (idx, nom_zone) AS (
    VALUES
        (1, 'A'), (2, 'B'), (3, 'C'), (4, 'D'), (5, 'E'), (6, 'F'), (7, 'G'), (8, 'H'),
        (9, 'I'), (10, 'J'), (11, 'K'), (12, 'L'), (13, 'M'), (14, 'N'), (15, 'O'), (16, 'P'),
        (17, 'Q'), (18, 'R'), (19, 'S'), (20, 'T'), (21, 'U'), (22, 'V'), (23, 'W'), (24, 'X'),
        (25, 'Y'), (26, 'Z'), (27, 'AA'), (28, 'AB'), (29, 'AC'), (30, 'AD')
)
INSERT INTO capteur (id_capteur, code_serie, id_zone)
SELECT
    uuid_generate_v4(),
    format('02:AA:BB:CC:DD:%s', lpad(upper(to_hex(capteur_seed.idx)), 2, '0')),
    zone.id_zone
FROM capteur_seed
JOIN zone
    ON zone.nom = capteur_seed.nom_zone
   AND zone.id_entrepot = '770e8400-e29b-41d4-a716-446655440011'
ON CONFLICT (id_zone) DO NOTHING;

