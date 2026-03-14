package fr.epsi.mspr2.adapters;

import fr.epsi.mspr2.adapters.jpa.JpaLotRepository;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.Entrepot;
import fr.epsi.mspr2.domain.ports.LotPort;
import fr.epsi.mspr2.adapters.jpa.JpaEntrepotRepository;
import fr.epsi.mspr2.config.EntrepotOwnerProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;



@Component
public class AdapterLot implements LotPort {
    private final JpaLotRepository jpaLotRepository;
    private final JpaEntrepotRepository jpaEntrepotRepository;
    private final EntrepotOwnerProperties entrepotOwnerProperties;

    public AdapterLot(JpaLotRepository jpaLotRepository,
                      JpaEntrepotRepository jpaEntrepotRepository,
                      EntrepotOwnerProperties entrepotOwnerProperties) {
        this.jpaLotRepository = jpaLotRepository;
        this.jpaEntrepotRepository = jpaEntrepotRepository;
        this.entrepotOwnerProperties = entrepotOwnerProperties;
    }

    @Override
    public void save(Lot lot) {
        UUID entrepotId = entrepotOwnerProperties.getEntrepotId();
        Entrepot entrepot = jpaEntrepotRepository.findById(entrepotId)
                .orElseThrow(() -> new IllegalStateException("Entrepot propriétaire introuvable pour l'id " + entrepotId));
        lot.setEntrepot(entrepot);
        jpaLotRepository.save(lot);
    }

    @Override
    public Lot findById(UUID id) {
        return jpaLotRepository.findById(id).orElse(null);
    }

    @Override
    public void delete(UUID id) {
        jpaLotRepository.deleteById(id);
    }

    public List<Lot> findAllByEntrepotOwner() {
        UUID entrepotId = entrepotOwnerProperties.getEntrepotId();
        Entrepot entrepot = jpaEntrepotRepository.findById(entrepotId)
                .orElseThrow(() -> new IllegalStateException("Entrepot propriétaire introuvable pour l'id " + entrepotId));
        return jpaLotRepository.findAllByEntrepot(entrepot);
    }
}