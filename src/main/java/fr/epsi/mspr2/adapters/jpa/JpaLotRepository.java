package fr.epsi.mspr2.adapters.jpa;

import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.Entrepot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface JpaLotRepository extends JpaRepository<Lot, UUID> {
    List<Lot> findAllByEntrepot(Entrepot entrepot);
}