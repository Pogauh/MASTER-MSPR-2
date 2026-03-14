package fr.epsi.mspr2.adapters.mapper;

import fr.epsi.mspr2.adapters.dto.LotResponseDTO;
import fr.epsi.mspr2.adapters.dto.LotStatusResponseDTO;
import fr.epsi.mspr2.domain.models.Lot;


public interface LotMapper {

    static LotResponseDTO toResponse(Lot lot) {
        LotResponseDTO dto = new LotResponseDTO();
        dto.idLot = lot.getIdLot();
        dto.status = lot.getStatus();
        dto.quantite = lot.getQuantite();
        dto.caracteristique = lot.getCaracteristique();

        dto.dateSortie = lot.getDateSortie();
        dto.dateStockage = lot.getDateStockage();
        dto.idExploitation = lot.getExploitation().getIdExploitation();
        dto.idEntrepot = lot.getEntrepot() != null ? lot.getEntrepot().getIdEntrepot() : null;
        return dto;
    }

    static LotStatusResponseDTO toStatusResponse(Lot lot) {
        LotStatusResponseDTO dto = new LotStatusResponseDTO();
        dto.idLot = lot.getIdLot();
        return dto;
    }
}