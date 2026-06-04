package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.ports.HistoriquePort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class HistoriqueService {

    private HistoriquePort historiquePort;

    public void createTransfereHistoriqueForLot(Lot lot, Lot lotRequest) {
        historiquePort.transfert(lot, lotRequest);
    }
}