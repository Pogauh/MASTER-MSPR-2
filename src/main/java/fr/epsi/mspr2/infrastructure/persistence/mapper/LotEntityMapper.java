package fr.epsi.mspr2.infrastructure.persistence.mapper;

import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.infrastructure.persistence.entity.LotEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = PersistenceMapperConfig.class,
        uses = {ZoneEntityMapper.class, ExploitationEntityMapper.class, HistoriqueEntityMapper.class})
public interface LotEntityMapper {

    /** Le lot avec sa zone, son exploitation et son historique. */
    Lot toDomain(LotEntity entity);

    /** Le lot avec sa zone et son exploitation, sans charger son historique. */
    @Mapping(target = "historique", ignore = true)
    Lot toDomainWithoutHistorique(LotEntity entity);

    /**
     * Reporte les champs propres au lot sur l'entité ; la zone et l'exploitation sont à rattacher par
     * l'appelant et l'historique est géré par ses propres entités.
     */
    @Mapping(target = "idLot", ignore = true)
    @Mapping(target = "zone", ignore = true)
    @Mapping(target = "exploitation", ignore = true)
    @Mapping(target = "historique", ignore = true)
    void updateEntity(Lot lot, @MappingTarget LotEntity entity);
}
