package fr.epsi.mspr2.infrastructure.adapters;

import fr.epsi.mspr2.infrastructure.jpa.JpaLotRepository;
import fr.epsi.mspr2.infrastructure.jpa.JpaExploitationRepository;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.Entrepot;
import fr.epsi.mspr2.domain.models.Exploitation;
import fr.epsi.mspr2.domain.ports.LotPort;
import fr.epsi.mspr2.infrastructure.jpa.JpaEntrepotRepository;
import fr.epsi.mspr2.infrastructure.config.EntrepotOwnerProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;



@Component
public class LotAdapter implements LotPort {
    private final JpaLotRepository jpaLotRepository;
    private final JpaEntrepotRepository jpaEntrepotRepository;
    private final JpaExploitationRepository jpaExploitationRepository;
    private final EntrepotOwnerProperties entrepotOwnerProperties;

    public LotAdapter(JpaLotRepository jpaLotRepository,
                      JpaEntrepotRepository jpaEntrepotRepository,
                      JpaExploitationRepository jpaExploitationRepository,
                      EntrepotOwnerProperties entrepotOwnerProperties) {
        this.jpaLotRepository = jpaLotRepository;
        this.jpaEntrepotRepository = jpaEntrepotRepository;
        this.jpaExploitationRepository = jpaExploitationRepository;
        this.entrepotOwnerProperties = entrepotOwnerProperties;
    }

    @Override
    public void save(Lot lot) {
        UUID exploitationId = lot.getExploitation() != null ? lot.getExploitation().getIdExploitation() : null;
        if (exploitationId == null) {
            throw new IllegalArgumentException("L'id de l'exploitation est obligatoire pour enregistrer un lot");
        }

        UUID entrepotId = entrepotOwnerProperties.getEntrepotId();
        Entrepot entrepot = jpaEntrepotRepository.findById(entrepotId)
                .orElseThrow(() -> new IllegalStateException("Entrepot propriétaire introuvable pour l'id " + entrepotId));
        Exploitation exploitation = jpaExploitationRepository.findById(exploitationId)
                .orElseThrow(() -> new IllegalStateException("Exploitation introuvable pour l'id " + exploitationId));

        lot.setEntrepot(entrepot);
        lot.setExploitation(exploitation);
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

    @Override
    public List<Lot> findAllWithAlerteAndDateSortieNullForEntrepotOwner() {
        UUID entrepotId = entrepotOwnerProperties.getEntrepotId();
        jpaEntrepotRepository.findById(entrepotId)
            .orElseThrow(() -> new IllegalStateException("Entrepot propriétaire introuvable pour l'id " + entrepotId));
        return jpaLotRepository.findAllWithAlerteAndDateSortieNullByEntrepotId(entrepotId);
    }
}