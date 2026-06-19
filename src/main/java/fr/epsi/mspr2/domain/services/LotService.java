package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.LotPort;
import fr.epsi.mspr2.infrastructure.dto.HistoriqueResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Service
public class LotService {
    
    private final LotPort lotPort;
    private final HistoriqueService historiqueService;

    public Lot createLot(Lot lot) {
        lotPort.save(lot);
        return lot;
    }
    
    public Lot getLotById(UUID idLot) {
        return lotPort.findById(idLot);
    }

    public Optional<List<Mesure>> getAllMesuresByLotId(UUID idLot) {
        return lotPort.findAllMesuresByLotId(idLot);
    }
    
    public Lot deleteLot(UUID idLot) {
        Lot lot = lotPort.findById(idLot);
        if (lot != null) {
            lotPort.delete(idLot);
        }
        return lot;
    }
    
    public List<Lot> getAllLotsForAllEntrepot() {
        return lotPort.findAllLotsForAllEntrepot();
    }

    public Integer getQuantiteTotale() {
        List<Lot> lots = lotPort.findAllLotsForAllEntrepot();
        return lots != null ? lots.stream()
                .map(Lot::getQuantite)
                .reduce(0, Integer::sum) : 0;
    }

    public Lot updateEmplacementLot(UUID idLot, Lot request) {
        Lot existingLot = lotPort.findById(idLot);
        if (existingLot == null) {
            return null;
        }
        historiqueService.createTransfereHistoriqueForLot(existingLot, request);

        return lotPort.updateEmplacementLot(existingLot, request);
    }

    public Lot updateStatusLot(UUID idLot, Lot request) {
        Lot existingLot = lotPort.findById(idLot);
        if (existingLot == null) {
            return null;
        }
        return lotPort.updateStatusLot(existingLot, request);
    }

    public Lot getOldestLot(){
        return lotPort.findOldestLot();
    }

    public Historique stockOut(UUID idLot){
        return lotPort.stockOut(idLot);
    }
}