package fr.epsi.mspr2.infrastructure.persistence.adapter;

import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.ports.MesurePort;
import fr.epsi.mspr2.infrastructure.persistence.entity.MesureEntity;
import fr.epsi.mspr2.infrastructure.persistence.mapper.MesureEntityMapper;
import fr.epsi.mspr2.infrastructure.persistence.repository.JpaCapteurRepository;
import fr.epsi.mspr2.infrastructure.persistence.repository.JpaMesureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MesureAdapter implements MesurePort {

    private final JpaMesureRepository mesureRepository;
    private final JpaCapteurRepository capteurRepository;
    private final MesureEntityMapper mapper;

    @Override
    @Transactional
    public Mesure save(Mesure mesure) {
        MesureEntity entity = mapper.toEntity(mesure);
        entity.setCapteur(capteurRepository.getReferenceById(mesure.getCapteur().getIdCapteur()));
        return mapper.toDomain(mesureRepository.save(entity));
    }

    @Override
    public Optional<Mesure> findById(UUID idMesure) {
        return mesureRepository.findById(idMesure).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteById(UUID idMesure) {
        mesureRepository.deleteById(idMesure);
    }

    @Override
    public List<Mesure> findByCapteurId(UUID idCapteur) {
        return mesureRepository.findByCapteurIdCapteur(idCapteur).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Mesure> findLatestByCapteurId(UUID idCapteur, int limit) {
        return mesureRepository
                .findByCapteurIdCapteurAndDateMesureIsNotNullOrderByDateMesureDesc(idCapteur, PageRequest.of(0, limit))
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Mesure> findByCapteurIdSince(UUID idCapteur, LocalDateTime since) {
        return mesureRepository.findByCapteurIdCapteurAndDateMesureGreaterThanEqual(idCapteur, since)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Mesure> findByZoneIdBetween(UUID idZone, LocalDateTime start, LocalDateTime end) {
        return mesureRepository.findAllByZoneIdAndDateMesureBetween(idZone, start, end)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Mesure> findLatestByZoneId(UUID idZone) {
        return mesureRepository.findFirstByCapteurZoneIdZoneOrderByDateMesureDesc(idZone).map(mapper::toDomain);
    }
}
