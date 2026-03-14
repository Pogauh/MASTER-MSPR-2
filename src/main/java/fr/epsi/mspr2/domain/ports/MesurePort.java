package fr.epsi.mspr2.domain.ports;

import fr.epsi.mspr2.domain.models.Mesure;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

public interface MesurePort {
        void save(Mesure mesure);
        Mesure findById(UUID idMesure);
        void delete(UUID idMesure);
        List<Mesure> findAllByEntrepot();
}