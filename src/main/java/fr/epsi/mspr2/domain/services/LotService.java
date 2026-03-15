package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.infrastructure.dto.LotResponseDTO;
import fr.epsi.mspr2.infrastructure.dto.LotRequestDTO;
import fr.epsi.mspr2.infrastructure.dto.LotStatusResponseDTO;
import fr.epsi.mspr2.infrastructure.mapper.LotMapper;
import fr.epsi.mspr2.domain.models.Exploitation;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.ports.LotPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class LotService {
    
    private final LotPort lotPort;
    
    public LotStatusResponseDTO createLot(LotRequestDTO request) {
        if (request.idExploitation == null) {
            throw new IllegalArgumentException("idExploitation est obligatoire");
        }

        Lot lot = new Lot();
        lot.setStatus(request.status);
        lot.setQuantite(request.quantite);
        lot.setCaracteristique(request.caracteristique);
        lot.setDateSortie(request.dateSortie);
        lot.setDateStockage(request.dateStockage);
        lot.setEmplacement(request.emplacement);

        Exploitation exploitation = new Exploitation();
        exploitation.setIdExploitation(request.idExploitation);
        lot.setExploitation(exploitation);

        lotPort.save(lot);
        return LotMapper.toStatusResponse(lot);
    }
    
    public LotResponseDTO getLotById(UUID idLot) {
        Lot lot = lotPort.findById(idLot);
        return lot != null ? LotMapper.toResponse(lot, false) : null;
    }
    
    public LotStatusResponseDTO deleteLot(UUID idLot) {
        Lot lot = lotPort.findById(idLot);
        if (lot != null) {
            lotPort.delete(idLot);
            return LotMapper.toStatusResponse(lot);
        }
        return null;
    }
    
    public List<LotResponseDTO> getAllLotsForEntrepot() {
        List<Lot> lot = lotPort.findAllByEntrepotOwner();
        return lot != null ? lot.stream().map(item -> LotMapper.toResponse(item, false)).toList() : List.of();
    }

    public List<LotResponseDTO> getLotsWithAlerteAndNoDateSortieForEntrepot() {
        List<Lot> lots = lotPort.findAllWithAlerteAndDateSortieNullForEntrepotOwner();
        return lots != null ? lots.stream().map(LotMapper::toResponse).toList() : List.of();
    }
}