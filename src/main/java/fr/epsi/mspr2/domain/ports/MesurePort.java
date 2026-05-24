package fr.epsi.mspr2.domain.ports;

import fr.epsi.mspr2.domain.models.Mesure;

import java.util.UUID;

public interface MesurePort {
        void save(Mesure mesure);
        Mesure findById(UUID idMesure);
        void delete(UUID idMesure);
}