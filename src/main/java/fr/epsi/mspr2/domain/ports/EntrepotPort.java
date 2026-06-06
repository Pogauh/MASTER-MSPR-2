package fr.epsi.mspr2.domain.ports;

import fr.epsi.mspr2.domain.models.Entrepot;
import fr.epsi.mspr2.domain.models.ZoneResponse;

import java.util.List;
import java.util.UUID;

public interface EntrepotPort {
    List<Entrepot> findAll();
    List<ZoneResponse> findZoneByIdEntrepot(UUID idEntrepot);
}

