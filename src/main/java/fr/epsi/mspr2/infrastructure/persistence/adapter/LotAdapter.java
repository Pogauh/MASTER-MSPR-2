package fr.epsi.mspr2.infrastructure.persistence.adapter;

import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.StatusEnum;
import fr.epsi.mspr2.domain.ports.LotPort;
import fr.epsi.mspr2.infrastructure.persistence.entity.LotEntity;
import fr.epsi.mspr2.infrastructure.persistence.mapper.LotEntityMapper;
import fr.epsi.mspr2.infrastructure.persistence.repository.JpaExploitationRepository;
import fr.epsi.mspr2.infrastructure.persistence.repository.JpaLotRepository;
import fr.epsi.mspr2.infrastructure.persistence.repository.JpaZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LotAdapter implements LotPort {

    private final JpaLotRepository lotRepository;
    private final JpaZoneRepository zoneRepository;
    private final JpaExploitationRepository exploitationRepository;
    private final LotEntityMapper mapper;

    @Override
    @Transactional
    public Lot save(Lot lot) {
        LotEntity entity = lot.getIdLot() == null
                ? new LotEntity()
                : lotRepository.findById(lot.getIdLot()).orElseGet(LotEntity::new);
        mapper.updateEntity(lot, entity);
        entity.setZone(zoneRepository.getReferenceById(lot.getZone().getIdZone()));
        entity.setExploitation(exploitationRepository.getReferenceById(lot.getExploitation().getIdExploitation()));
        return mapper.toDomainWithoutHistorique(lotRepository.save(entity));
    }

    @Override
    public Optional<Lot> findById(UUID idLot) {
        return lotRepository.findByIdWithHistorique(idLot).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteById(UUID idLot) {
        lotRepository.deleteById(idLot);
    }

    @Override
    public List<Lot> findAll() {
        return lotRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Lot> findByZoneId(UUID idZone) {
        return lotRepository.findByZoneIdZone(idZone).stream().map(mapper::toDomainWithoutHistorique).toList();
    }

    @Override
    public List<Lot> findByDateProductionBefore(LocalDate date) {
        return lotRepository.findByDateProductionBefore(date).stream().map(mapper::toDomainWithoutHistorique).toList();
    }

    @Override
    public Optional<Lot> findOldestByStatus(StatusEnum status) {
        return lotRepository.findFirstByStatusOrderByDateStockageAsc(status).map(mapper::toDomain);
    }

    @Override
    public long count() {
        return lotRepository.count();
    }

    @Override
    public long countByStatusIn(Collection<StatusEnum> statuses) {
        return lotRepository.countByStatusIn(statuses);
    }

    @Override
    public OptionalDouble findAverageAgeInDays() {
        Double moyenne = lotRepository.findAverageAgeInDays();
        return moyenne != null ? OptionalDouble.of(moyenne) : OptionalDouble.empty();
    }

    @Override
    public List<UUID> findIdsByDateStockageBetween(LocalDateTime start, LocalDateTime end) {
        return lotRepository.findIdsByDateStockageBetween(start, end);
    }
}
