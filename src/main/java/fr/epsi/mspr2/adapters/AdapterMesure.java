package fr.epsi.mspr2.adapters;

import fr.epsi.mspr2.adapters.jpa.JpaEntrepotRepository;
import fr.epsi.mspr2.adapters.jpa.JpaMesureRepository;
import fr.epsi.mspr2.config.EntrepotOwnerProperties;
import fr.epsi.mspr2.domain.models.Entrepot;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.MesurePort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Component
public class AdapterMesure  implements MesurePort {

    private final JpaMesureRepository jpaMesureRepository;
    private final JpaEntrepotRepository jpaEntrepotRepository;
    private final EntrepotOwnerProperties entrepotOwnerProperties;

    @Override
    public void save(Mesure mesure) {
        UUID entrepotId = entrepotOwnerProperties.getEntrepotId();
        Entrepot entrepot = jpaEntrepotRepository.findById(entrepotId)
                .orElseThrow(() -> new IllegalStateException("Entrepot propriétaire introuvable pour l'id " + entrepotId));
        mesure.setEntrepot(entrepot);
        jpaMesureRepository.save(mesure);
    }

    @Override
    public Mesure findById(UUID idMesure) {
        return jpaMesureRepository.findById(idMesure).orElse(null);
    }

    @Override
    public void delete(UUID idMesure) {
        jpaMesureRepository.deleteById(idMesure);
    }

    @Override
    public List<Mesure> findAllByEntrepot() {
        UUID entrepotId = entrepotOwnerProperties.getEntrepotId();
        Entrepot entrepot = jpaEntrepotRepository.findById(entrepotId)
                .orElseThrow(() -> new IllegalStateException("Entrepot propriétaire introuvable pour l'id " + entrepotId));
        return jpaMesureRepository.findAllByEntrepot(entrepot);
    }
}
