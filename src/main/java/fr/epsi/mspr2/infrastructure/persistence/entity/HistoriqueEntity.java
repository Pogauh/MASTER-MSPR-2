package fr.epsi.mspr2.infrastructure.persistence.entity;

import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historique")
@Getter
@Setter
@NoArgsConstructor
public class HistoriqueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_historique", updatable = false, nullable = false)
    private UUID idHistorique;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50)
    private TypeHistoriqueEnum type;

    @Column(name = "date_emission")
    private LocalDateTime dateEmission;

    @Column(name = "zone_precedente")
    private UUID zonePrecedente;

    @Column(name = "zone_actuelle")
    private UUID zoneActuelle;

    @Column(name = "detail", length = 255)
    private String detail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lot", nullable = false)
    private LotEntity lot;
}
