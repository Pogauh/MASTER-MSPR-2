package fr.epsi.mspr2.infrastructure.mapper;

import fr.epsi.mspr2.domain.models.Exploitation;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.Zone;
import fr.epsi.mspr2.infrastructure.dto.LotRequestDTO;
import fr.epsi.mspr2.infrastructure.dto.LotResponseDTO;
import fr.epsi.mspr2.infrastructure.dto.LotStatusResponseDTO;


public interface LotMapper {

    static LotResponseDTO toResponse(Lot lot) {
        LotResponseDTO dto = new LotResponseDTO();
        dto.idLot = lot.getIdLot();
        dto.status = lot.getStatus();
        dto.quantite = lot.getQuantite();
        dto.caracteristique = lot.getCaracteristique();

        dto.dateSortie = lot.getDateSortie();
        dto.dateStockage = lot.getDateStockage();
        dto.emplacement = lot.getEmplacement();
        dto.idExploitation = lot.getExploitation() != null ? lot.getExploitation().getIdExploitation() : null;
        dto.idEntrepot = lot.getEntrepot() != null ? lot.getEntrepot().getIdEntrepot() : null;
        dto.historique = lot.getHistorique();
        return dto;
    }

    static LotStatusResponseDTO toStatusResponse(Lot lot) {
        LotStatusResponseDTO dto = new LotStatusResponseDTO();
        dto.idLot = lot.getIdLot();
        return dto;
    }

    static Lot toEntity(LotRequestDTO request) {
        if (request == null) {
            return null;
        }

        Lot lot = new Lot();
        lot.setStatus(request.status);
        lot.setQuantite(request.quantite);
        lot.setCaracteristique(request.caracteristique);
        Zone zone = new Zone();
        zone.setIdZone(request.idZone);
        lot.setZone(zone);
        Exploitation exploitation = new Exploitation();
        exploitation.setIdExploitation(request.idExploitation);
        lot.setExploitation(exploitation);

        return lot;
    }
}