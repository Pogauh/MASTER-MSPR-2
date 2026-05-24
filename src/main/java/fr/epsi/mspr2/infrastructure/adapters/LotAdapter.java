package fr.epsi.mspr2.infrastructure.adapters;

import fr.epsi.mspr2.infrastructure.jpa.JpaLotRepository;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.ports.LotPort;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class LotAdapter implements LotPort {
    private final JpaLotRepository jpaLotRepository;

    public LotAdapter(JpaLotRepository jpaLotRepository) {
        this.jpaLotRepository = jpaLotRepository;
    }

    @Override
    public void save(Lot lot) {
        UUID exploitationId = lot.getExploitation() != null ? lot.getExploitation().getIdExploitation() : null;
        if (exploitationId == null) {
            throw new IllegalArgumentException("L'id de l'exploitation est obligatoire pour enregistrer un lot");
        }
        lot.setDateStockage(LocalDateTime.now());
        jpaLotRepository.save(lot);
    }

    @Override
    public Lot findById(UUID id) {
        return jpaLotRepository.findByIdWithHistorique(id).orElse(null);
    }

    @Override
    public void delete(UUID id) {
        jpaLotRepository.deleteById(id);
    }

    public List<Lot> findAllLotsForAllEntrepot() {
        return jpaLotRepository.findAll();
    }
}