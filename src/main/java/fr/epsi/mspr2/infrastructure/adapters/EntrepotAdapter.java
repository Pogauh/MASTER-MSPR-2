package fr.epsi.mspr2.infrastructure.adapters;

import fr.epsi.mspr2.domain.models.Entrepot;
import fr.epsi.mspr2.domain.ports.EntrepotPort;
import fr.epsi.mspr2.infrastructure.jpa.JpaEntrepotRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EntrepotAdapter implements EntrepotPort {
    private final JpaEntrepotRepository jpaEntrepotRepository;

    public EntrepotAdapter(JpaEntrepotRepository jpaEntrepotRepository) {
        this.jpaEntrepotRepository = jpaEntrepotRepository;
    }

    @Override
    public List<Entrepot> findAll() {
        return jpaEntrepotRepository.findAll();
    }
}

