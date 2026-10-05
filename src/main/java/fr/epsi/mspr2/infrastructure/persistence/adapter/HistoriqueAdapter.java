package fr.epsi.mspr2.infrastructure.persistence.adapter;

import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.domain.ports.HistoriquePort;
import fr.epsi.mspr2.infrastructure.persistence.entity.HistoriqueEntity;
import fr.epsi.mspr2.infrastructure.persistence.mapper.HistoriqueEntityMapper;
import fr.epsi.mspr2.infrastructure.persistence.repository.JpaHistoriqueRepository;
import fr.epsi.mspr2.infrastructure.persistence.repository.JpaLotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistoriqueAdapter implements HistoriquePort {

    private final JpaHistoriqueRepository historiqueRepository;
    private final JpaLotRepository lotRepository;
    private final HistoriqueEntityMapper mapper;

    @Override
    @Transactional
    public Historique save(Historique historique) {
        HistoriqueEntity entity = mapper.toEntity(historique);
        entity.setLot(lotRepository.getReferenceById(historique.getIdLot()));
        return mapper.toDomain(historiqueRepository.save(entity));
    }

    @Override
    public List<Historique> findByLotId(UUID idLot) {
        return historiqueRepository.findAllByLotId(idLot).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Historique> findByLotIdAndType(UUID idLot, TypeHistoriqueEnum type) {
        return historiqueRepository.findAllByLotIdAndType(idLot, type).stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countByTypeAfter(TypeHistoriqueEnum type, LocalDateTime date) {
        return historiqueRepository.countByDateEmissionAfterAndType(date, type);
    }
}
