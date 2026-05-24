package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Entrepot;
import fr.epsi.mspr2.domain.ports.EntrepotPort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class EntrepotService {
    private final EntrepotPort entrepotPort;

    public List<Entrepot> getAllEntrepots() {
        return entrepotPort.findAll();
    }
}

