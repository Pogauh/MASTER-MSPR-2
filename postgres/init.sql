CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE pays (
                      id_pays UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                      nom VARCHAR(50) NOT NULL,
                      temp_ideale DECIMAL(5,2),
                      tolerance_temp DECIMAL(3,2),
                      humi_ideale DECIMAL(5,2),
                      tolerance_humi DECIMAL(3,2),
                      email_responsable VARCHAR(100)
);

CREATE TABLE exploitation (
                              id_exploitation UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                              nom VARCHAR(50) NOT NULL,
                              adresse VARCHAR(150),
                              id_pays UUID REFERENCES pays(id_pays)
);

CREATE TABLE entrepot (
                          id_entrepot UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          nom VARCHAR(50) NOT NULL,
                          localisation VARCHAR(150),
                          id_pays UUID REFERENCES pays(id_pays)
);

CREATE TABLE lot (
                     id_lot UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                     status VARCHAR(20) DEFAULT 'CONFORME',
                     quantite INT,
                     caracteristique INT,
                     date_stockage TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                     date_sortie TIMESTAMP,
                     id_exploitation UUID REFERENCES exploitation(id_exploitation),
                     id_entrepot UUID REFERENCES entrepot(id_entrepot)
);

CREATE TABLE mesure (
                        id_mesure UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        temperature DECIMAL(5,2),
                        humidite DECIMAL(5,2),
                        date_mesure TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        id_entrepot UUID REFERENCES entrepot(id_entrepot)
);

CREATE TABLE alerte (
                        id_alerte UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        type_alerte VARCHAR(50),
                        description TEXT,
                        date_emission TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE genere_alerte (
                               id_lot UUID REFERENCES lot(id_lot),
                               id_alerte UUID REFERENCES alerte(id_alerte),
                               PRIMARY KEY (id_lot, id_alerte)
);

INSERT INTO pays (id_pays, nom, temp_ideale, tolerance_temp, humi_ideale, tolerance_humi, email_responsable)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'Brésil', 29.0, 3.0, 55.0, 2.0, 'resp.bresil@futurekawa.com');

INSERT INTO exploitation (id_exploitation, nom, adresse, id_pays)
VALUES ('aa0e8400-e29b-41d4-a716-446655440022', 'Fazenda Santa Maria', 'Minas Gerais, Brésil', '550e8400-e29b-41d4-a716-446655440000');

INSERT INTO entrepot (id_entrepot, nom, localisation, id_pays)
VALUES ('770e8400-e29b-41d4-a716-446655440011', 'Entrepôt Principal Santos', 'Port de Santos, Brésil', '550e8400-e29b-41d4-a716-446655440000');