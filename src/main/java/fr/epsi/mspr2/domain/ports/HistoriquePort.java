package fr.epsi.mspr2.domain.ports;

import fr.epsi.mspr2.domain.models.Lot;

public interface HistoriquePort {

    void transfert(Lot lot, Lot lotRequest);
}