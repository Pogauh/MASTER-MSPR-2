package fr.epsi.mspr2.domain.ports;

import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;

import java.util.List;
import java.util.UUID;

public interface HistoriquePort {

    void transfert(Lot lot, Lot lotRequest);

    List<Historique> findAllHistoriqueForLot(UUID idLot);

    List<Historique> findAllHistoriqueForLotByType(UUID idLot, TypeHistoriqueEnum type);
}