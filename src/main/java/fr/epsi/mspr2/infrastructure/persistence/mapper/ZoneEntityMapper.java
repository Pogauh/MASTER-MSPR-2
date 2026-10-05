package fr.epsi.mspr2.infrastructure.persistence.mapper;

import fr.epsi.mspr2.domain.models.Zone;
import fr.epsi.mspr2.infrastructure.persistence.entity.ZoneEntity;
import org.mapstruct.Mapper;

@Mapper(config = PersistenceMapperConfig.class, uses = EntrepotEntityMapper.class)
public interface ZoneEntityMapper {

    Zone toDomain(ZoneEntity entity);
}
