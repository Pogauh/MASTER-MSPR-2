package fr.epsi.mspr2.infrastructure.mapper;

import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.infrastructure.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LotMapper {


    @Mapping(target = "emplacement", source = "zone.idZone")
    @Mapping(target = "idExploitation", source = "exploitation.idExploitation")
    @Mapping(target = "idEntrepot", source = "entrepot.idEntrepot")
    LotResponseDTO toResponse(Lot lot);

    LotStatusResponseDTO toStatusResponse(Lot lot);

    @Mapping(target = "zone.idZone", source = "idZone")
    @Mapping(target = "exploitation.idExploitation", source = "idExploitation")
    @Mapping(target = "idLot", ignore = true)
    @Mapping(target = "dateSortie", ignore = true)
    @Mapping(target = "dateStockage", ignore = true)
    @Mapping(target = "historique", ignore = true)
    @Mapping(target = "entrepot.idEntrepot", ignore = true)
    Lot lotToEntity(LotRequestDTO request);

    @Mapping(target = "idLot", ignore = true)
    @Mapping(target = "quantite", ignore = true)
    @Mapping(target = "caracteristique", ignore = true)
    @Mapping(target = "dateSortie", ignore = true)
    @Mapping(target = "dateStockage", ignore = true)
    @Mapping(target = "dateProduction", ignore = true)
    @Mapping(target = "exploitation", ignore = true)
    @Mapping(target = "zone", ignore = true)
    @Mapping(target = "historique", ignore = true)
    Lot lotStatusToEntity(LotStatusRequestDTO request);

    @Mapping(target = "zone.idZone", source = "idZone")
    @Mapping(target = "idLot", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "quantite", ignore = true)
    @Mapping(target = "caracteristique", ignore = true)
    @Mapping(target = "dateSortie", ignore = true)
    @Mapping(target = "dateStockage", ignore = true)
    @Mapping(target = "dateProduction", ignore = true)
    @Mapping(target = "exploitation", ignore = true)
    @Mapping(target = "historique", ignore = true)
    Lot lotEmplacementToEntity(LotEmplacementRequestDTO request);
}