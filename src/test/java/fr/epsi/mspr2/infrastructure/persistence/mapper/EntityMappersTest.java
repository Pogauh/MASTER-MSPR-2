package fr.epsi.mspr2.infrastructure.persistence.mapper;

import fr.epsi.mspr2.domain.models.Alerte;
import fr.epsi.mspr2.domain.models.AlerteDetails;
import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.models.TypeAlerteEnum;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.infrastructure.persistence.entity.AlerteEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.CapteurEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.EntrepotEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.HistoriqueEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.LotEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.MesureEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.ZoneEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig({MesureEntityMapperImpl.class, AlerteEntityMapperImpl.class, HistoriqueEntityMapperImpl.class,
        CapteurEntityMapperImpl.class, ZoneEntityMapperImpl.class, EntrepotEntityMapperImpl.class})
class EntityMappersTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 3, 15, 10, 0);

    @Autowired
    MesureEntityMapper mesureMapper;
    @Autowired
    AlerteEntityMapper alerteMapper;
    @Autowired
    HistoriqueEntityMapper historiqueMapper;
    @Autowired
    CapteurEntityMapper capteurMapper;

    private CapteurEntity capteur() {
        EntrepotEntity entrepot = new EntrepotEntity();
        entrepot.setIdEntrepot(UUID.randomUUID());
        entrepot.setNom("Principal");
        ZoneEntity zone = new ZoneEntity();
        zone.setIdZone(UUID.randomUUID());
        zone.setNom("A");
        zone.setEntrepot(entrepot);
        CapteurEntity capteur = new CapteurEntity();
        capteur.setIdCapteur(UUID.randomUUID());
        capteur.setCodeSerie("02:AA:BB:CC:DD:01");
        capteur.setZone(zone);
        return capteur;
    }

    // ---------------------------------------------------------------- mesure
    @Test
    void mesure_toDomain_gardeLeCapteurParSonIdentifiantSeulement() {
        CapteurEntity capteur = capteur();
        MesureEntity entite = new MesureEntity();
        entite.setIdMesure(UUID.randomUUID());
        entite.setTemperature(new BigDecimal("29.38"));
        entite.setHumidite(new BigDecimal("54.18"));
        entite.setDateMesure(T0);
        entite.setCapteur(capteur);

        Mesure mesure = mesureMapper.toDomain(entite);

        assertThat(mesure.getIdMesure()).isEqualTo(entite.getIdMesure());
        assertThat(mesure.getTemperature()).isEqualByComparingTo("29.38");
        assertThat(mesure.getHumidite()).isEqualByComparingTo("54.18");
        assertThat(mesure.getDateMesure()).isEqualTo(T0);
        assertThat(mesure.getCapteur()).isEqualTo(Capteur.builder().idCapteur(capteur.getIdCapteur()).build());
    }

    @Test
    void mesure_toEntity_laisseLeCapteurAuxSoinsDeLAdaptateur() {
        Mesure mesure = Mesure.builder().temperature(new BigDecimal("35")).humidite(new BigDecimal("55")).dateMesure(T0)
                .capteur(Capteur.builder().idCapteur(UUID.randomUUID()).build()).build();

        MesureEntity entite = mesureMapper.toEntity(mesure);

        assertThat(entite.getIdMesure()).isNull();
        assertThat(entite.getTemperature()).hasToString("35");
        assertThat(entite.getHumidite()).hasToString("55");
        assertThat(entite.getDateMesure()).isEqualTo(T0);
        assertThat(entite.getCapteur()).isNull();
    }

    @Test
    void mesure_sansCapteur() {
        assertThat(mesureMapper.toDomain(new MesureEntity()).getCapteur()).isNull();
    }

    // ---------------------------------------------------------------- alerte
    @Test
    void alerte_toDomain_etToEntity() {
        CapteurEntity capteur = capteur();
        AlerteEntity entite = new AlerteEntity();
        entite.setIdAlerte(UUID.randomUUID());
        entite.setTypeAlerte(TypeAlerteEnum.BASSETEMPERATURE);
        entite.setDescription("trop froid");
        entite.setDateEmission(T0);
        entite.setCapteur(capteur);

        Alerte alerte = alerteMapper.toDomain(entite);

        assertThat(alerte.getIdAlerte()).isEqualTo(entite.getIdAlerte());
        assertThat(alerte.getTypeAlerte()).isEqualTo(TypeAlerteEnum.BASSETEMPERATURE);
        assertThat(alerte.getDescription()).isEqualTo("trop froid");
        assertThat(alerte.getDateEmission()).isEqualTo(T0);
        assertThat(alerte.getCapteur()).isEqualTo(Capteur.builder().idCapteur(capteur.getIdCapteur()).build());

        AlerteEntity retour = alerteMapper.toEntity(alerte);
        assertThat(retour.getIdAlerte()).isEqualTo(entite.getIdAlerte());
        assertThat(retour.getTypeAlerte()).isEqualTo(TypeAlerteEnum.BASSETEMPERATURE);
        assertThat(retour.getDescription()).isEqualTo("trop froid");
        assertThat(retour.getDateEmission()).isEqualTo(T0);
        assertThat(retour.getCapteur()).isNull();
    }

    @Test
    void alerte_toDetails_laZoneEstCelleDuCapteur() {
        CapteurEntity capteur = capteur();
        AlerteEntity entite = new AlerteEntity();
        entite.setIdAlerte(UUID.randomUUID());
        entite.setTypeAlerte(TypeAlerteEnum.HAUTEHUMIDITE);
        entite.setDescription("trop humide");
        entite.setDateEmission(T0);
        entite.setCapteur(capteur);

        assertThat(alerteMapper.toDetails(entite)).isEqualTo(new AlerteDetails(
                entite.getIdAlerte(), TypeAlerteEnum.HAUTEHUMIDITE, "trop humide", T0, capteur.getZone().getIdZone()));
    }

    @Test
    void alerte_toDetails_sansCapteurPasDeZone() {
        AlerteEntity entite = new AlerteEntity();
        entite.setTypeAlerte(TypeAlerteEnum.EXPIRED);

        assertThat(alerteMapper.toDetails(entite).idZone()).isNull();
    }

    // ---------------------------------------------------------------- historique
    @Test
    void historique_toDomain_etToEntity() {
        LotEntity lot = new LotEntity();
        lot.setIdLot(UUID.randomUUID());
        UUID zoneA = UUID.randomUUID();
        UUID zoneB = UUID.randomUUID();
        HistoriqueEntity entite = new HistoriqueEntity();
        entite.setIdHistorique(UUID.randomUUID());
        entite.setType(TypeHistoriqueEnum.TRANSFERT);
        entite.setDateEmission(T0);
        entite.setZonePrecedente(zoneA);
        entite.setZoneActuelle(zoneB);
        entite.setDetail("transfert");
        entite.setLot(lot);

        Historique historique = historiqueMapper.toDomain(entite);

        assertThat(historique).isEqualTo(Historique.builder().idHistorique(entite.getIdHistorique()).type(TypeHistoriqueEnum.TRANSFERT)
                .dateEmission(T0).zonePrecedente(zoneA).zoneActuelle(zoneB).detail("transfert").idLot(lot.getIdLot()).build());

        HistoriqueEntity retour = historiqueMapper.toEntity(historique);
        assertThat(retour.getIdHistorique()).isEqualTo(entite.getIdHistorique());
        assertThat(retour.getType()).isEqualTo(TypeHistoriqueEnum.TRANSFERT);
        assertThat(retour.getDateEmission()).isEqualTo(T0);
        assertThat(retour.getZonePrecedente()).isEqualTo(zoneA);
        assertThat(retour.getZoneActuelle()).isEqualTo(zoneB);
        assertThat(retour.getDetail()).isEqualTo("transfert");
        assertThat(retour.getLot()).isNull();
    }

    // ---------------------------------------------------------------- capteur
    @Test
    void capteur_toDomain_avecSaZoneEtSonEntrepot() {
        CapteurEntity entite = capteur();

        Capteur capteur = capteurMapper.toDomain(entite);

        assertThat(capteur.getIdCapteur()).isEqualTo(entite.getIdCapteur());
        assertThat(capteur.getCodeSerie()).isEqualTo("02:AA:BB:CC:DD:01");
        assertThat(capteur.getZone().getNom()).isEqualTo("A");
        assertThat(capteur.getEntrepot().getNom()).isEqualTo("Principal");
    }
}
