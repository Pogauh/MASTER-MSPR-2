package fr.epsi.mspr2.infrastructure.adapters;

import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.infrastructure.jpa.JpaHistoriqueRepository;
import fr.epsi.mspr2.infrastructure.jpa.JpaLotRepository;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.ports.LotPort;
import fr.epsi.mspr2.infrastructure.jpa.JpaMesureRepository;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class LotAdapter implements LotPort {
    private final JpaLotRepository jpaLotRepository;
    private final JpaMesureRepository jpaMesureRepository;
    private final JpaHistoriqueRepository historiqueJpaRepository;

    public LotAdapter(JpaLotRepository jpaLotRepository, JpaMesureRepository jpaMesureRepository, JpaHistoriqueRepository historiqueJpaRepository) {
        this.jpaLotRepository = jpaLotRepository;
        this.jpaMesureRepository = jpaMesureRepository;
        this.historiqueJpaRepository = historiqueJpaRepository;
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

    @Override
    public Lot updateEmplacementLot(Lot lot, Lot lotRequest) {
        if (lotRequest.getZone() != null && lotRequest.getZone().getIdZone() != null) {
            lot.setZone(lotRequest.getZone());
        }
        return jpaLotRepository.save(lot);
    }

    @Override
    public Lot updateStatusLot(Lot lot, Lot lotRequest) {
        if (lotRequest.getStatus() != null) {
            lot.setStatus(lotRequest.getStatus());
        }
        return jpaLotRepository.save(lot);
    }

    @Override
    public Optional<List<Mesure>> findAllMesuresByLotId(UUID idLot) {
        Lot lot = findById(idLot);
        List<Historique> historiques = historiqueJpaRepository.findAllHistoriqueForLotByType(idLot, TypeHistoriqueEnum.TRANSFERT);

        List<Mesure> mesuresHistorique = new ArrayList<>();
        LocalDateTime curseurDate = lot.getDateStockage();

        if (historiques != null && !historiques.isEmpty()) {

            for (Historique historique : historiques) {
                jpaMesureRepository.findAllByZoneIdAndDateMesureBetween(historique.getZonePrecedente(), curseurDate, historique.getDateEmission())
                        .ifPresent(mesuresHistorique::addAll);

                curseurDate = historique.getDateEmission();
            }
        }

        jpaMesureRepository.findAllByZoneIdAndDateMesureBetween(lot.getZone().getIdZone(), curseurDate, LocalDateTime.now())
                .ifPresent(mesuresHistorique::addAll);

        return mesuresHistorique.isEmpty() ? Optional.empty() : Optional.of(mesuresHistorique);
    }
}