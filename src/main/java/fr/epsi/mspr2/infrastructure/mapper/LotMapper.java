package fr.epsi.mspr2.infrastructure.mapper;

import fr.epsi.mspr2.domain.models.Exploitation;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.Zone;
import fr.epsi.mspr2.infrastructure.dto.*;
import org.springframework.stereotype.Component;


@Component
public class LotMapper {

    public LotResponseDTO toResponse(Lot lot) {
        LotResponseDTO dto = new LotResponseDTO();
        dto.idLot = lot.getIdLot();
        dto.status = String.valueOf(lot.getStatus());
        dto.quantite = lot.getQuantite();
        dto.caracteristique = lot.getCaracteristique();

        dto.dateSortie = lot.getDateSortie();
        dto.dateStockage = lot.getDateStockage();
        dto.dateProduction = lot.getDateProduction();
        dto.emplacement = lot.getZone().getIdZone();
        dto.idExploitation = lot.getExploitation() != null ? lot.getExploitation().getIdExploitation() : null;
        dto.idEntrepot = lot.getEntrepot() != null ? lot.getEntrepot().getIdEntrepot() : null;
        dto.historique = lot.getHistorique();
        return dto;
    }

    public LotStatusResponseDTO toStatusResponse(Lot lot) {
        LotStatusResponseDTO dto = new LotStatusResponseDTO();
        dto.idLot = lot.getIdLot();
        return dto;
    }

    public Lot lotToEntity(LotRequestDTO request) {
        if (request == null) {
            return null;
        }

        Lot lot = new Lot();
        lot.setStatus(Lot.StatusEnum.valueOf(request.status));
        lot.setQuantite(request.quantite);
        lot.setCaracteristique(request.caracteristique);
        Zone zone = new Zone();
        zone.setIdZone(request.idZone);
        lot.setZone(zone);
        lot.setDateProduction(request.dateProduction);
        Exploitation exploitation = new Exploitation();
        exploitation.setIdExploitation(request.idExploitation);
        lot.setExploitation(exploitation);

        return lot;
    }

    public Lot lotStatusToEntity(LotStatusRequestDTO request) {
        if (request == null) {
            return null;
        }
        Lot lot = new Lot();
        lot.setStatus(Lot.StatusEnum.valueOf(request.status));

        return lot;
    }

    public Lot lotEmplacementToEntity(LotEmplacementRequestDTO request) {
        if (request == null) {
            return null;
        }
        Lot lot = new Lot();
        Zone zone = new Zone();
        zone.setIdZone(request.idZone);
        lot.setZone(zone);

        return lot;
    }
}