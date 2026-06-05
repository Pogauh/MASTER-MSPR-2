package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.ports.HistoriquePort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class HistoriqueService {

    private HistoriquePort historiquePort;

    public void createTransfereHistoriqueForLot(Lot lot, Lot lotRequest) {
        historiquePort.transfert(lot, lotRequest);
    }

    public List<Historique> findAllHistoriquesForLot(UUID idLot) {
        return historiquePort.findAllHistoriqueForLot(idLot);
    }

    public List<Historique> findAllHistoriquesForLotByType(UUID idLot, fr.epsi.mspr2.domain.models.TypeHistoriqueEnum type) {
        return historiquePort.findAllHistoriqueForLotByType(idLot, type);
    }
}