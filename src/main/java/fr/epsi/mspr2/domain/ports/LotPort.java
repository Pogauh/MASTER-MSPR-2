package fr.epsi.mspr2.domain.ports;

import fr.epsi.mspr2.domain.models.Lot;

import java.util.List;
import java.util.UUID;

public interface LotPort {
        void save(Lot lot);
        Lot findById(UUID id_lot);
        void delete(UUID id_lot);
        List<Lot> findAllLotsForAllEntrepot();
        Lot updateEmplacementLot(Lot lot,Lot lotRequest);
}