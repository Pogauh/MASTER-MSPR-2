package fr.epsi.mspr2.infrastructure.persistence.mapper;

import fr.epsi.mspr2.domain.models.Entrepot;
import fr.epsi.mspr2.infrastructure.persistence.entity.EntrepotEntity;
import org.mapstruct.Mapper;

@Mapper(config = PersistenceMapperConfig.class)
public interface EntrepotEntityMapper {

    Entrepot toDomain(EntrepotEntity entity);
}
