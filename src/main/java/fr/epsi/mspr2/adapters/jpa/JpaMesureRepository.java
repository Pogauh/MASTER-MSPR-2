package fr.epsi.mspr2.adapters.jpa;

import fr.epsi.mspr2.domain.models.Entrepot;
import fr.epsi.mspr2.domain.models.Mesure;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface JpaMesureRepository extends JpaRepository<Mesure, UUID> {
    List<Mesure> findAllByEntrepot(Entrepot entrepot);
}

