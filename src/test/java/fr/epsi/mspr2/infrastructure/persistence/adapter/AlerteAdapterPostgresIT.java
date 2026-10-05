package fr.epsi.mspr2.infrastructure.persistence.adapter;

import fr.epsi.mspr2.containers.OnPostgres;

/** Rejoue AlerteAdapterIT sur un vrai PostgreSQL (ignoré sans Docker). */
@OnPostgres
class AlerteAdapterPostgresIT extends AlerteAdapterIT {
}
