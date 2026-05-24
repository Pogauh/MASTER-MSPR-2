package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.infrastructure.dto.MesureRequestDTO;
import fr.epsi.mspr2.infrastructure.dto.MesureResponseDTO;
import fr.epsi.mspr2.infrastructure.dto.MesureStatusResponseDTO;
import fr.epsi.mspr2.infrastructure.mapper.MesureMapper;
import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.MesurePort;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class MesureService {

    private final MesurePort mesurePort;
    private final AlerteService alerteService;

    public MesureStatusResponseDTO createMesure(MesureRequestDTO dto) {
        if (dto.idCapteur == null) {
            throw new IllegalArgumentException("idCapteur est obligatoire");
        }
        Capteur capteur = new Capteur();
        capteur.setIdCapteur(dto.idCapteur);
        Mesure mesure = new Mesure();
        mesure.setTemperature(dto.temperature);
        mesure.setHumidite(dto.humidite);
        mesure.setDateMesure(dto.dateMesure);
        mesure.setCapteur(capteur);
        mesurePort.save(mesure);
        alerteService.sendAlerte(mesure);
        return MesureMapper.toStatusResponse(mesure);
    }

    public MesureResponseDTO getMesureById(UUID idMesure) {
        Mesure mesure = mesurePort.findById(idMesure);
        return mesure != null ? MesureMapper.toResponse(mesure) : null;
    }

    public MesureStatusResponseDTO deleteMesure(UUID idMesure) {
        Mesure mesure = mesurePort.findById(idMesure);
        if (mesure != null) {
            mesurePort.delete(idMesure);
            return MesureMapper.toStatusResponse(mesure);
        }
        return null;
    }
}