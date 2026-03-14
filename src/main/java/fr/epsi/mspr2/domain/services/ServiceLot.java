package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.adapters.dto.LotResponseDTO;
import fr.epsi.mspr2.adapters.dto.LotStatusResponseDTO;
import fr.epsi.mspr2.adapters.mapper.LotMapper;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.ports.LotPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class ServiceLot {
    
    private final LotPort lotRepository;
    
    public LotStatusResponseDTO createLot(Lot lot) {
        lotRepository.save(lot);
        return LotMapper.toStatusResponse(lot);
    }
    
    public LotResponseDTO getLotById(UUID idLot) {
        Lot lot = lotRepository.findById(idLot);
        return lot != null ? LotMapper.toResponse(lot) : null;
    }
    
    public LotStatusResponseDTO deleteLot(UUID idLot) {
        Lot lot = lotRepository.findById(idLot);
        if (lot != null) {
            lotRepository.delete(idLot);
            return LotMapper.toStatusResponse(lot);
        }
        return null;
    }
    
    public List<LotResponseDTO> getAllLotsForEntrepot() {
        List<Lot> lot = lotRepository.findAllByEntrepotOwner();
        return lot != null ? lot.stream().map(LotMapper::toResponse).toList() : List.of();
    }
}