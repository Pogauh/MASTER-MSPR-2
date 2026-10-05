package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.domain.ports.HistoriquePort;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class HistoriqueService {

    private final HistoriquePort historiquePort;

    public List<Historique> findAllHistoriquesForLot(UUID idLot) {
        return historiquePort.findByLotId(idLot);
    }

    public List<Historique> findAllHistoriquesForLotByType(UUID idLot, TypeHistoriqueEnum type) {
        return historiquePort.findByLotIdAndType(idLot, type);
    }
}
