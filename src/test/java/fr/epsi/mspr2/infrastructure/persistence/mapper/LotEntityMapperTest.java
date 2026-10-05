package fr.epsi.mspr2.infrastructure.persistence.mapper;

import fr.epsi.mspr2.domain.models.Exploitation;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.StatusEnum;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.domain.models.Zone;
import fr.epsi.mspr2.infrastructure.persistence.entity.EntrepotEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.ExploitationEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.HistoriqueEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.LotEntity;
import fr.epsi.mspr2.infrastructure.persistence.entity.ZoneEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig({LotEntityMapperImpl.class, ZoneEntityMapperImpl.class, EntrepotEntityMapperImpl.class,
        ExploitationEntityMapperImpl.class, HistoriqueEntityMapperImpl.class})
class LotEntityMapperTest {

    @Autowired
    LotEntityMapper mapper;

    private LotEntity entite() {
        EntrepotEntity entrepot = new EntrepotEntity();
        entrepot.setIdEntrepot(UUID.randomUUID());
        entrepot.setNom("Principal");
        entrepot.setLocalisation("Bresil");
        ZoneEntity zone = new ZoneEntity();
        zone.setIdZone(UUID.randomUUID());
        zone.setNom("A");
        zone.setEntrepot(entrepot);
        ExploitationEntity exploitation = new ExploitationEntity();
        exploitation.setIdExploitation(UUID.randomUUID());
        exploitation.setNom("Flamenco");
        exploitation.setAdresse("804 rue du Barista");

        LotEntity lot = new LotEntity();
        lot.setIdLot(UUID.randomUUID());
        lot.setStatus(StatusEnum.ALERT);
        lot.setQuantite(100);
        lot.setCaracteristique(2);
        lot.setDateSortie(LocalDateTime.of(2026, 1, 2, 3, 4));
        lot.setDateStockage(LocalDateTime.of(2025, 5, 6, 7, 8));
        lot.setDateProduction(LocalDate.of(2025, 4, 1));
        lot.setZone(zone);
        lot.setExploitation(exploitation);

        HistoriqueEntity historique = new HistoriqueEntity();
        historique.setIdHistorique(UUID.randomUUID());
        historique.setType(TypeHistoriqueEnum.ALERTE);
        historique.setDateEmission(LocalDateTime.of(2025, 6, 1, 0, 0));
        historique.setZonePrecedente(zone.getIdZone());
        historique.setZoneActuelle(zone.getIdZone());
        historique.setDetail("alerte");
        historique.setLot(lot);
        lot.setHistorique(List.of(historique));
        return lot;
    }

    @Test
    void toDomain_reprendLeLotSaZoneSonEntrepotSonExploitationEtSonHistorique() {
        LotEntity entite = entite();

        Lot lot = mapper.toDomain(entite);

        assertThat(lot.getIdLot()).isEqualTo(entite.getIdLot());
        assertThat(lot.getStatus()).isEqualTo(StatusEnum.ALERT);
        assertThat(lot.getQuantite()).isEqualTo(100);
        assertThat(lot.getCaracteristique()).isEqualTo(2);
        assertThat(lot.getDateSortie()).isEqualTo(LocalDateTime.of(2026, 1, 2, 3, 4));
        assertThat(lot.getDateStockage()).isEqualTo(LocalDateTime.of(2025, 5, 6, 7, 8));
        assertThat(lot.getDateProduction()).isEqualTo(LocalDate.of(2025, 4, 1));
        assertThat(lot.getZone().getIdZone()).isEqualTo(entite.getZone().getIdZone());
        assertThat(lot.getZone().getNom()).isEqualTo("A");
        assertThat(lot.getEntrepot().getIdEntrepot()).isEqualTo(entite.getZone().getEntrepot().getIdEntrepot());
        assertThat(lot.getEntrepot().getNom()).isEqualTo("Principal");
        assertThat(lot.getEntrepot().getLocalisation()).isEqualTo("Bresil");
        assertThat(lot.getExploitation().getNom()).isEqualTo("Flamenco");
        assertThat(lot.getHistorique()).singleElement().satisfies(h -> {
            assertThat(h.getIdHistorique()).isEqualTo(entite.getHistorique().get(0).getIdHistorique());
            assertThat(h.getType()).isEqualTo(TypeHistoriqueEnum.ALERTE);
            assertThat(h.getIdLot()).isEqualTo(entite.getIdLot());
            assertThat(h.getDetail()).isEqualTo("alerte");
        });
    }

    @Test
    void toDomainWithoutHistorique_neChargePasLHistorique() {
        Lot lot = mapper.toDomainWithoutHistorique(entite());

        assertThat(lot.getHistorique()).isEmpty();
        assertThat(lot.getZone().getNom()).isEqualTo("A");
        assertThat(lot.getExploitation().getNom()).isEqualTo("Flamenco");
    }

    @Test
    void toDomain_nul() {
        assertThat(mapper.toDomain(null)).isNull();
        assertThat(mapper.toDomainWithoutHistorique(null)).isNull();
    }

    @Test
    void updateEntity_reporteLesChampsDuLotSansToucherAuxRelationsNiAIdentifiant() {
        LotEntity entite = entite();
        UUID idOrigine = entite.getIdLot();
        ZoneEntity zoneOrigine = entite.getZone();
        ExploitationEntity exploitationOrigine = entite.getExploitation();
        List<HistoriqueEntity> historiqueOrigine = entite.getHistorique();
        Lot lot = Lot.builder()
                .idLot(UUID.randomUUID())
                .status(StatusEnum.SHIPPED)
                .quantite(5)
                .caracteristique(4)
                .dateSortie(LocalDateTime.of(2026, 3, 15, 10, 0))
                .dateStockage(LocalDateTime.of(2026, 3, 1, 0, 0))
                .dateProduction(LocalDate.of(2026, 2, 1))
                .zone(Zone.builder().idZone(UUID.randomUUID()).build())
                .exploitation(Exploitation.builder().idExploitation(UUID.randomUUID()).build())
                .build();

        mapper.updateEntity(lot, entite);

        assertThat(entite.getStatus()).isEqualTo(StatusEnum.SHIPPED);
        assertThat(entite.getQuantite()).isEqualTo(5);
        assertThat(entite.getCaracteristique()).isEqualTo(4);
        assertThat(entite.getDateSortie()).isEqualTo(LocalDateTime.of(2026, 3, 15, 10, 0));
        assertThat(entite.getDateStockage()).isEqualTo(LocalDateTime.of(2026, 3, 1, 0, 0));
        assertThat(entite.getDateProduction()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(entite.getIdLot()).isEqualTo(idOrigine);
        assertThat(entite.getZone()).isSameAs(zoneOrigine);
        assertThat(entite.getExploitation()).isSameAs(exploitationOrigine);
        assertThat(entite.getHistorique()).isSameAs(historiqueOrigine);
    }
}
