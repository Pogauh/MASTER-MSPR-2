package fr.epsi.mspr2.domain.ports;

import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.Mesure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LotPort {
        void save(Lot lot);
        Lot findById(UUID id_lot);
        void delete(UUID id_lot);
        List<Lot> findAllLotsForAllEntrepot();
        Lot updateEmplacementLot(Lot lot,Lot lotRequest);
        Lot updateStatusLot(Lot lot,Lot lotRequest);
        Optional<List<Mesure>> findAllMesuresByLotId(UUID idLot);
        Lot findOldestLot();
}