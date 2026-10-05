package fr.epsi.mspr2.api.mapper;

import fr.epsi.mspr2.api.dto.MesureLotResponseDTO;
import fr.epsi.mspr2.api.dto.MesureRequestDTO;
import fr.epsi.mspr2.api.dto.MesureResponseDTO;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.models.NouvelleMesure;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = ApiMapperConfig.class)
public interface MesureMapper {

    @Mapping(target = "idCapteur", source = "capteur.idCapteur")
    MesureResponseDTO toResponse(Mesure mesure);

    MesureLotResponseDTO toLotResponse(Mesure mesure);

    NouvelleMesure toNouvelleMesure(MesureRequestDTO request);
}
