package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.infrastructure.dto.LotResponseDTO;
import fr.epsi.mspr2.infrastructure.dto.LotRequestDTO;
import fr.epsi.mspr2.infrastructure.dto.LotStatusResponseDTO;
import fr.epsi.mspr2.infrastructure.mapper.LotMapper;
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
        Lot lot = LotMapper.toEntity(request);
        lotPort.save(lot);
        return LotMapper.toStatusResponse(lot);
    }
    
    public LotResponseDTO getLotById(UUID idLot) {
        Lot lot = lotPort.findById(idLot);
        return lot != null ? LotMapper.toResponse(lot) : null;
    }
    
    public LotStatusResponseDTO deleteLot(UUID idLot) {
        Lot lot = lotPort.findById(idLot);
        if (lot != null) {
            lotPort.delete(idLot);
            return LotMapper.toStatusResponse(lot);
        }
        return null;
    }
    
    public List<LotResponseDTO> getAllLotsForAllEntrepot() {
        List<Lot> lot = lotPort.findAllLotsForAllEntrepot();
        return lot != null ? lot.stream()
                .map(LotMapper::toResponse)
                .toList() : List.of();
    }

    public Integer getQuantiteTotale() {
        List<Lot> lots = lotPort.findAllLotsForAllEntrepot();
        return lots != null ? lots.stream()
                .map(Lot::getQuantite)
                .reduce(0, Integer::sum) : 0;
    }
}