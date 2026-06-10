package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.MesureRequest;
import fr.epsi.mspr2.infrastructure.dto.MesureResponseDTO;
import fr.epsi.mspr2.infrastructure.dto.MesureStatusResponseDTO;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.MesurePort;

import fr.epsi.mspr2.infrastructure.mapper.MesureMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@AllArgsConstructor
@Service
public class MesureService {

    private final MesurePort mesurePort;
    private final AlerteService alerteService;
    private final MesureMapper mesureMapper;

    public MesureStatusResponseDTO createMesure(MesureRequest mesureRequest) {
        if (mesureRequest.idCapteur == null) {
            throw new IllegalArgumentException("idCapteur est obligatoire");
        }

        mesurePort.save(mesureMapper.mesureRequestToMesureEntity(mesureRequest));
        alerteService.sendAlerte(mesureMapper.mesureRequestToMesureEntity(mesureRequest));
        return mesureMapper.toStatusResponse(mesureMapper.mesureRequestToMesureEntity(mesureRequest));
    }

    public MesureResponseDTO getMesureById(UUID idMesure) {
        Mesure mesure = mesurePort.findById(idMesure);
        return mesure != null ? mesureMapper.toResponse(mesure) : null;
    }

    public MesureStatusResponseDTO deleteMesure(UUID idMesure) {
        Mesure mesure = mesurePort.findById(idMesure);
        if (mesure != null) {
            mesurePort.delete(idMesure);
            return mesureMapper.toStatusResponse(mesure);
        }
        return null;
    }
}