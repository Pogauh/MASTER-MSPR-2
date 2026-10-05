package fr.epsi.mspr2.api.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/** Configuration commune des mappers DTO &lt;-&gt; modèle du domaine : un champ non mappé est une erreur de compilation. */
@MapperConfig(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ApiMapperConfig {
}
