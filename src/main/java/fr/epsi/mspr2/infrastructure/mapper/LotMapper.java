package fr.epsi.mspr2.infrastructure.mapper;

import fr.epsi.mspr2.infrastructure.dto.AlerteResponseDTO;
import fr.epsi.mspr2.infrastructure.dto.LotResponseDTO;
import fr.epsi.mspr2.infrastructure.dto.LotStatusResponseDTO;
import fr.epsi.mspr2.domain.models.Alerte;
import fr.epsi.mspr2.domain.models.GenereAlerte;
import fr.epsi.mspr2.domain.models.Lot;

import java.util.Comparator;
import java.util.List;


public interface LotMapper {

    static LotResponseDTO toResponse(Lot lot) {
        return toResponse(lot, true);
    }

    static LotResponseDTO toResponse(Lot lot, boolean includeAlertes) {
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
        dto.alertes = includeAlertes ? toAlerteResponses(lot.getGenereAlertes()) : List.of();
        return dto;
    }

    static LotStatusResponseDTO toStatusResponse(Lot lot) {
        LotStatusResponseDTO dto = new LotStatusResponseDTO();
        dto.idLot = lot.getIdLot();
        return dto;
    }

    private static List<AlerteResponseDTO> toAlerteResponses(List<GenereAlerte> genereAlertes) {
        if (genereAlertes == null || genereAlertes.isEmpty()) {
            return List.of();
        }

        return genereAlertes.stream()
            .map(GenereAlerte::getAlerte)
            .filter(java.util.Objects::nonNull)
            .sorted(Comparator.comparing(Alerte::getDateEmission, Comparator.nullsLast(Comparator.reverseOrder())))
            .map(LotMapper::toAlerteResponse)
            .toList();
    }

    private static AlerteResponseDTO toAlerteResponse(Alerte source) {
        AlerteResponseDTO target = new AlerteResponseDTO();
        target.idAlerte = source.getIdAlerte();
        target.typeAlerte = source.getTypeAlerte() != null ? source.getTypeAlerte().name() : null;
        target.description = source.getDescription();
        target.dateEmission = source.getDateEmission();
        return target;
    }
}