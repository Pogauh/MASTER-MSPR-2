package fr.epsi.mspr2.infrastructure.persistence.adapter;

import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.domain.ports.CapteurPort;
import fr.epsi.mspr2.infrastructure.persistence.mapper.CapteurEntityMapper;
import fr.epsi.mspr2.infrastructure.persistence.repository.JpaCapteurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CapteurAdapter implements CapteurPort {

    private final JpaCapteurRepository capteurRepository;
    private final CapteurEntityMapper mapper;

    @Override
    public Optional<Capteur> findById(UUID idCapteur) {
        return capteurRepository.findByCapteurId(idCapteur).map(mapper::toDomain);
    }

    @Override
    public List<Capteur> findByEntrepotId(UUID idEntrepot) {
        return capteurRepository.findAllByEntrepotId(idEntrepot).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<UUID> findAllIds() {
        return capteurRepository.findAllIds();
    }
}
