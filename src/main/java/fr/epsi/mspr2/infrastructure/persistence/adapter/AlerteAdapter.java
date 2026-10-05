package fr.epsi.mspr2.infrastructure.persistence.adapter;

import fr.epsi.mspr2.domain.models.Alerte;
import fr.epsi.mspr2.domain.models.AlerteDetails;
import fr.epsi.mspr2.domain.ports.AlertePort;
import fr.epsi.mspr2.infrastructure.persistence.entity.AlerteEntity;
import fr.epsi.mspr2.infrastructure.persistence.mapper.AlerteEntityMapper;
import fr.epsi.mspr2.infrastructure.persistence.repository.JpaAlerteRepository;
import fr.epsi.mspr2.infrastructure.persistence.repository.JpaCapteurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlerteAdapter implements AlertePort {

    private final JpaAlerteRepository alerteRepository;
    private final JpaCapteurRepository capteurRepository;
    private final AlerteEntityMapper mapper;

    @Override
    @Transactional
    public Alerte save(Alerte alerte) {
        AlerteEntity entity = mapper.toEntity(alerte);
        if (alerte.getCapteur() != null) {
            entity.setCapteur(capteurRepository.getReferenceById(alerte.getCapteur().getIdCapteur()));
        }
        return mapper.toDomain(alerteRepository.save(entity));
    }

    @Override
    public List<AlerteDetails> findDetailsByEntrepotId(UUID idEntrepot, LocalDateTime start, LocalDateTime end) {
        return alerteRepository.findByEntrepotId(idEntrepot, start, end).stream().map(mapper::toDetails).toList();
    }

    @Override
    public int countByDateEmissionBetween(LocalDateTime start, LocalDateTime end) {
        return alerteRepository.countByDateEmissionBetween(start, end);
    }
}
