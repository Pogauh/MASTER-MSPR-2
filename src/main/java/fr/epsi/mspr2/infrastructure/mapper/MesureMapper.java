package fr.epsi.mspr2.infrastructure.mapper;

import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.models.MesureRequest;
import fr.epsi.mspr2.infrastructure.dto.MesureRequestDTO;
import fr.epsi.mspr2.infrastructure.dto.MesureResponseDTO;
import fr.epsi.mspr2.infrastructure.dto.MesureStatusResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MesureMapper {

    @Mapping(target = "idCapteur", source = "capteur.idCapteur")
    MesureResponseDTO toResponse(Mesure mesure);

    MesureStatusResponseDTO toStatusResponse(Mesure mesure);

    MesureRequest mesureRequestDtoToMesureRequest(MesureRequestDTO request);

    @Mapping(source = "idCapteur", target = "capteur.idCapteur")
    Mesure mesureRequestToMesureEntity(MesureRequest mesureRequest);
}