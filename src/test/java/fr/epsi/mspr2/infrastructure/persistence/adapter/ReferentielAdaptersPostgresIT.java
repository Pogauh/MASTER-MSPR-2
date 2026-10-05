package fr.epsi.mspr2.infrastructure.persistence.adapter;

import fr.epsi.mspr2.containers.OnPostgres;

/** Rejoue ReferentielAdaptersIT sur un vrai PostgreSQL (ignoré sans Docker). */
@OnPostgres
class ReferentielAdaptersPostgresIT extends ReferentielAdaptersIT {
}
