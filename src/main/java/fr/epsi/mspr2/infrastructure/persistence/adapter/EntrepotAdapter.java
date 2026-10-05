package fr.epsi.mspr2.infrastructure.persistence.adapter;

import fr.epsi.mspr2.domain.models.Entrepot;
import fr.epsi.mspr2.domain.models.ZoneSummary;
import fr.epsi.mspr2.domain.ports.EntrepotPort;
import fr.epsi.mspr2.infrastructure.persistence.mapper.EntrepotEntityMapper;
import fr.epsi.mspr2.infrastructure.persistence.repository.JpaEntrepotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EntrepotAdapter implements EntrepotPort {

    private final JpaEntrepotRepository entrepotRepository;
    private final EntrepotEntityMapper mapper;

    @Override
    public List<Entrepot> findAll() {
        return entrepotRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<ZoneSummary> findZoneSummariesByEntrepotId(UUID idEntrepot) {
        return entrepotRepository.findZoneRowsByEntrepotId(idEntrepot).stream()
                .map(row -> new ZoneSummary(row.idZone(), row.nom(), row.idEntrepot(), row.idCapteur(), row.lotCount()))
                .toList();
    }
}
