package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.infrastructure.dto.MesureResponseDTO;
import fr.epsi.mspr2.infrastructure.mapper.MesureMapper;
import fr.epsi.mspr2.domain.ports.CapteurPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class CapteurService {

    private final CapteurPort capteurPort;
    private final MesureMapper mesureMapper;

    public List<MesureResponseDTO> getMesuresByCapteurId(UUID capteurId) {
        if (capteurId == null) {
            throw new IllegalArgumentException("capteurId est obligatoire");
        }
        return capteurPort.findMesuresByCapteurId(capteurId)
            .stream()
            .map(mesureMapper::toResponse)
            .toList();
    }
}