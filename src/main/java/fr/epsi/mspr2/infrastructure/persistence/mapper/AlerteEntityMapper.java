package fr.epsi.mspr2.infrastructure.persistence.mapper;

import fr.epsi.mspr2.domain.models.Alerte;
import fr.epsi.mspr2.domain.models.AlerteDetails;
import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.infrastructure.persistence.entity.AlerteEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.CapteurEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = PersistenceMapperConfig.class)
public interface AlerteEntityMapper {

    /** Le capteur de l'alerte n'est restitué que par son identifiant. */
    @Mapping(target = "capteur", source = "capteur", qualifiedByName = "capteurParSonIdentifiant")
    Alerte toDomain(AlerteEntity entity);

    /** Le capteur est à rattacher par l'appelant. */
    @Mapping(target = "capteur", ignore = true)
    AlerteEntity toEntity(Alerte alerte);

    /** L'alerte, rattachée à la zone du capteur qui l'a déclenchée (le capteur doit être renseigné). */
    @Mapping(target = "idZone", source = "capteur.zone.idZone")
    AlerteDetails toDetails(AlerteEntity entity);

    @Named("capteurParSonIdentifiant")
    default Capteur capteurParSonIdentifiant(CapteurEntity capteur) {
        return capteur == null ? null : Capteur.builder().idCapteur(capteur.getIdCapteur()).build();
    }
}
