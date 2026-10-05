package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.MesurePort;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class CapteurService {

    private final MesurePort mesurePort;

    public List<Mesure> getMesuresByCapteurId(UUID capteurId) {
        if (capteurId == null) {
            throw new IllegalArgumentException("capteurId est obligatoire");
        }
        return mesurePort.findByCapteurId(capteurId);
    }
}
