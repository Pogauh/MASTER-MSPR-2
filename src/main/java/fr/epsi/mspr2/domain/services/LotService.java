package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.exceptions.LotIntrouvableException;
import fr.epsi.mspr2.domain.exceptions.TransfertLotInvalideException;
import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.models.StatusEnum;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.domain.ports.HistoriquePort;
import fr.epsi.mspr2.domain.ports.LotPort;
import fr.epsi.mspr2.domain.ports.MesurePort;
import fr.epsi.mspr2.domain.ports.TransactionPort;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class LotService {

    private final LotPort lotPort;
    private final HistoriquePort historiquePort;
    private final MesurePort mesurePort;
    private final TransactionPort transactionPort;
    private final Clock clock;

    /** Enregistre l'entrée en stock d'un lot : il doit provenir d'une exploitation et être rangé dans une zone. */
    public Lot createLot(Lot lot) {
        if (lot.getExploitation() == null || lot.getExploitation().getIdExploitation() == null) {
            throw new IllegalArgumentException("L'id de l'exploitation est obligatoire pour enregistrer un lot");
        }
        if (lot.getZone() == null || lot.getZone().getIdZone() == null) {
            throw new IllegalArgumentException("L'id de la zone est obligatoire pour enregistrer un lot");
        }
        lot.enregistrerStockage(LocalDateTime.now(clock));
        return lotPort.save(lot);
    }

    public Optional<Lot> getLotById(UUID idLot) {
        return lotPort.findById(idLot);
    }

    /** Tous les lots, du plus anciennement stocké au plus récent. */
    public List<Lot> getAllLotsForAllEntrepot() {
        return lotPort.findAll().stream()
                .sorted(Comparator.comparing(Lot::getDateStockage, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public int getQuantiteTotale() {
        return lotPort.findAll().stream()
                .map(Lot::getQuantite)
                .mapToInt(quantite -> quantite != null ? quantite : 0)
                .sum();
    }

    /** @return le lot supprimé, ou vide s'il n'existe pas */
    public Optional<Lot> deleteLot(UUID idLot) {
        Optional<Lot> lot = lotPort.findById(idLot);
        lot.ifPresent(l -> lotPort.deleteById(idLot));
        return lot;
    }

    /**
     * Transfère le lot dans une autre zone et garde la trace du transfert dans son historique.
     *
     * @return le lot déplacé, ou vide s'il n'existe pas
     * @throws TransfertLotInvalideException si la zone de destination est absente ou identique à la zone actuelle
     */
    public Optional<Lot> updateEmplacementLot(UUID idLot, UUID idZoneDestination) {
        Optional<Lot> existant = lotPort.findById(idLot);
        if (existant.isEmpty()) {
            return Optional.empty();
        }
        Lot lot = existant.get();

        if (lot.getZone() == null || idZoneDestination == null) {
            throw new TransfertLotInvalideException("Erreur de transfert : l'un des emplacement est null.");
        }
        if (lot.getZone().getIdZone().equals(idZoneDestination)) {
            throw new TransfertLotInvalideException(
                    "Erreur de transfert : l'emplacement de destination est identique à l'emplacement actuel.");
        }

        return Optional.of(transactionPort.execute(() -> {
            historiquePort.save(Historique.transfert(lot, idZoneDestination, LocalDateTime.now(clock)));
            lot.deplacerVers(idZoneDestination);
            return lotPort.save(lot);
        }));
    }

    /**
     * Change le statut du lot ; sans nouveau statut le lot est inchangé.
     *
     * @return le lot, ou vide s'il n'existe pas
     */
    public Optional<Lot> updateStatusLot(UUID idLot, StatusEnum status) {
        return lotPort.findById(idLot).map(lot -> {
            if (status != null) {
                lot.setStatus(status);
            }
            return lotPort.save(lot);
        });
    }

    /** Le lot conforme stocké depuis le plus longtemps : c'est le prochain à expédier (FIFO). */
    public Optional<Lot> getOldestLot() {
        return lotPort.findOldestByStatus(StatusEnum.COMPLIANT);
    }

    /**
     * Expédie le lot et garde la trace de la sortie de stock.
     *
     * @throws LotIntrouvableException si le lot n'existe pas
     */
    public Historique stockOut(UUID idLot) {
        return transactionPort.execute(() -> {
            Lot lot = lotPort.findById(idLot).orElseThrow(() -> new LotIntrouvableException(idLot));
            LocalDateTime maintenant = LocalDateTime.now(clock);
            lot.expedier(maintenant);
            lotPort.save(lot);
            return historiquePort.save(Historique.expedition(idLot, maintenant));
        });
    }

    /**
     * Relevés qui ont concerné le lot depuis son entrée en stock : pour chaque zone traversée,
     * les mesures effectuées pendant que le lot s'y trouvait.
     *
     * @return les mesures, vide si le lot est inconnu ou si aucune mesure ne le concerne
     */
    public List<Mesure> getAllMesuresByLotId(UUID idLot) {
        Optional<Lot> existant = lotPort.findById(idLot);
        if (existant.isEmpty()) {
            return List.of();
        }
        Lot lot = existant.get();

        List<Historique> transferts = historiquePort.findByLotIdAndType(idLot, TypeHistoriqueEnum.TRANSFERT).stream()
                .sorted(Comparator.comparing(Historique::getDateEmission, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        List<Mesure> mesures = new ArrayList<>();
        LocalDateTime debutSejour = lot.getDateStockage();
        for (Historique transfert : transferts) {
            mesures.addAll(mesurePort.findByZoneIdBetween(transfert.getZonePrecedente(), debutSejour, transfert.getDateEmission()));
            debutSejour = transfert.getDateEmission();
        }
        mesures.addAll(mesurePort.findByZoneIdBetween(lot.getZone().getIdZone(), debutSejour, LocalDateTime.now(clock)));
        return mesures;
    }
}
