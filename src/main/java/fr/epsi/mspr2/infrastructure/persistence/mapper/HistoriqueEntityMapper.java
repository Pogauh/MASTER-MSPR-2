package fr.epsi.mspr2.infrastructure.persistence.mapper;

import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.infrastructure.persistence.entity.HistoriqueEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = PersistenceMapperConfig.class)
public interface HistoriqueEntityMapper {

    @Mapping(target = "idLot", source = "lot.idLot")
    Historique toDomain(HistoriqueEntity entity);

    /** Le lot est à rattacher par l'appelant. */
    @Mapping(target = "lot", ignore = true)
    HistoriqueEntity toEntity(Historique historique);
}
