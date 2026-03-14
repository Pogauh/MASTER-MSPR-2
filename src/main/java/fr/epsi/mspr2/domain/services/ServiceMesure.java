package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.adapters.dto.MesureRequestDTO;
import fr.epsi.mspr2.adapters.dto.MesureResponseDTO;
import fr.epsi.mspr2.adapters.dto.MesureStatusResponseDTO;
import fr.epsi.mspr2.adapters.mapper.MesureMapper;
import fr.epsi.mspr2.config.EntrepotOwnerProperties;
import fr.epsi.mspr2.domain.models.Entrepot;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.MesurePort;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class ServiceMesure {

    private final MesurePort mesurePort;
    private final EntrepotOwnerProperties entrepotOwnerProperties;

    public MesureStatusResponseDTO createMesure(MesureRequestDTO dto) {
        Entrepot entrepot = new Entrepot();
        entrepot.setIdEntrepot(entrepotOwnerProperties.getEntrepotId());
        Mesure mesure = new Mesure();
        mesure.setTemperature(dto.temperature);
        mesure.setHumidite(dto.humidite);
        mesure.setDateMesure(dto.dateMesure);
        mesure.setEntrepot(entrepot);
        mesurePort.save(mesure);
        return MesureMapper.toStatusResponse(mesure);
    }

    public List<MesureResponseDTO> getAllMesuresForEntrepot() {
        return mesurePort.findAllByEntrepot()
            .stream()
            .map(MesureMapper::toResponse)
            .toList();
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