package fr.epsi.mspr2.api.mapper;

import fr.epsi.mspr2.api.dto.LotRequestDTO;
import fr.epsi.mspr2.api.dto.LotResponseDTO;
import fr.epsi.mspr2.domain.models.Exploitation;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.StatusEnum;
import fr.epsi.mspr2.domain.models.Zone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(config = ApiMapperConfig.class, uses = HistoriqueMapper.class)
public interface LotMapper {

    @Mapping(target = "emplacement", source = "zone.idZone")
    @Mapping(target = "idExploitation", source = "exploitation.idExploitation")
    @Mapping(target = "idEntrepot", source = "entrepot.idEntrepot")
    LotResponseDTO toResponse(Lot lot);

    /** Le lot à créer : l'exploitation et la zone ne sont désignées que par leur identifiant. */
    @Mapping(target = "idLot", ignore = true)
    @Mapping(target = "dateSortie", ignore = true)
    @Mapping(target = "dateStockage", ignore = true)
    @Mapping(target = "historique", ignore = true)
    @Mapping(target = "exploitation", source = "idExploitation")
    @Mapping(target = "zone", source = "idZone")
    Lot toLot(LotRequestDTO request);

    default Zone zoneParSonIdentifiant(UUID idZone) {
        return idZone == null ? null : Zone.builder().idZone(idZone).build();
    }

    default Exploitation exploitationParSonIdentifiant(UUID idExploitation) {
        return idExploitation == null ? null : Exploitation.builder().idExploitation(idExploitation).build();
    }

    /** @return le statut désigné, ou {@code null} si la requête n'en précise pas */
    default StatusEnum toStatus(String status) {
        return status == null ? null : StatusEnum.valueOf(status);
    }
}
