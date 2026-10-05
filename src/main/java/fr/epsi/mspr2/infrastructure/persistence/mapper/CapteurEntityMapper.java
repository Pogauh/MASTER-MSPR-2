package fr.epsi.mspr2.infrastructure.persistence.mapper;

import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.infrastructure.persistence.entity.CapteurEntity;
import org.mapstruct.Mapper;

@Mapper(config = PersistenceMapperConfig.class, uses = ZoneEntityMapper.class)
public interface CapteurEntityMapper {

    /** Le capteur avec sa zone et l'entrepôt de celle-ci. */
    Capteur toDomain(CapteurEntity entity);
}
