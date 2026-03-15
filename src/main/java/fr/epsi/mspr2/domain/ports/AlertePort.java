package fr.epsi.mspr2.domain.ports;

import fr.epsi.mspr2.domain.models.Mesure;

public interface AlertePort {
    void sendAlerte(Mesure mesure);

    boolean isCompatible(Mesure mesure);
}
