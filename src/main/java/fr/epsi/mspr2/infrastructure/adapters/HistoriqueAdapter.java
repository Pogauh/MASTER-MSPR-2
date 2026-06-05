package fr.epsi.mspr2.infrastructure.adapters;

import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.domain.ports.HistoriquePort;
import fr.epsi.mspr2.infrastructure.jpa.JpaHistoriqueRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class HistoriqueAdapter implements HistoriquePort {

    private final JpaHistoriqueRepository historiqueJpaRepository;

    public HistoriqueAdapter(JpaHistoriqueRepository historiqueJpaRepository) {
        this.historiqueJpaRepository = historiqueJpaRepository;
    }

     public void transfert(Lot lot, Lot lotRequest) {
        if (lot.getZone() == null || lotRequest.getZone() == null) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erreur de transfert : l'un des emplacement est null.");
        } else if (lot.getZone().getIdZone().equals(lotRequest.getZone().getIdZone())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erreur de transfert : l'emplacement de destination est identique à l'emplacement actuel.");
        }
        
        Historique historique = new Historique();
        historique.setType(TypeHistoriqueEnum.TRANSFERT);
        historique.setDateEmission(LocalDateTime.now());
        historique.setZonePrecedente(lot.getZone().getIdZone());
        historique.setZoneActuelle(lotRequest.getZone().getIdZone());
        historique.setDetail("Transfert du lot " + lot.getIdLot() + " de la zone " + lot.getZone().getIdZone() + " vers la zone " + lotRequest.getZone().getIdZone());
        historique.setLot(lot);
        historiqueJpaRepository.save(historique);
    }

    @Override
    public List<Historique> findAllHistoriqueForLot(UUID idLot) {
        return historiqueJpaRepository.findAllHistoriqueForLot(idLot);
    }

    @Override
    public List<Historique> findAllHistoriqueForLotByType(UUID idLot, TypeHistoriqueEnum type) {
        return historiqueJpaRepository.findAllHistoriqueForLotByType(idLot, type);
    }
}
