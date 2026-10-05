package fr.epsi.mspr2.infrastructure.persistence.mapper;

import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.infrastructure.persistence.entity.CapteurEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.MesureEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = PersistenceMapperConfig.class)
public interface MesureEntityMapper {

    /** Le capteur d'une mesure n'est restitué que par son identifiant : cela évite de charger sa zone pour chaque mesure. */
    @Mapping(target = "capteur", source = "capteur", qualifiedByName = "capteurParSonIdentifiant")
    Mesure toDomain(MesureEntity entity);

    /** Le capteur est à rattacher par l'appelant. */
    @Mapping(target = "capteur", ignore = true)
    MesureEntity toEntity(Mesure mesure);

    @Named("capteurParSonIdentifiant")
    default Capteur capteurParSonIdentifiant(CapteurEntity capteur) {
        return capteur == null ? null : Capteur.builder().idCapteur(capteur.getIdCapteur()).build();
    }
}
