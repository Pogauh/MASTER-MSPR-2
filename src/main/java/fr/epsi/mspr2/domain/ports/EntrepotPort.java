package fr.epsi.mspr2.domain.ports;

import fr.epsi.mspr2.domain.models.Entrepot;
import fr.epsi.mspr2.domain.models.ZoneSummary;

import java.util.List;
import java.util.UUID;

public interface EntrepotPort {

    List<Entrepot> findAll();

    List<ZoneSummary> findZoneSummariesByEntrepotId(UUID idEntrepot);
}
