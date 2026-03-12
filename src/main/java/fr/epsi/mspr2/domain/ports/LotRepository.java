package fr.epsi.mspr2.domain.ports;

import fr.epsi.mspr2.domain.models.Lot;

public interface LotRepository {
        void save(Lot lot);
        Lot findById(String id);
        void delete(Lot lot);
}
