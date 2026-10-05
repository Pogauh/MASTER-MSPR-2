package fr.epsi.mspr2.infrastructure.persistence.entity;

import fr.epsi.mspr2.domain.models.StatusEnum;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "lot")
@Getter
@Setter
@NoArgsConstructor
public class LotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_lot", updatable = false, nullable = false)
    private UUID idLot;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private StatusEnum status;

    @Column(name = "quantite")
    private Integer quantite;

    @Column(name = "caracteristique")
    private Integer caracteristique;

    @Column(name = "date_sortie")
    private LocalDateTime dateSortie;

    @Column(name = "date_stockage")
    private LocalDateTime dateStockage;

    @Column(name = "date_production")
    private LocalDate dateProduction;

    @ManyToOne
    @JoinColumn(name = "id_exploitation", nullable = false)
    private ExploitationEntity exploitation;

    @ManyToOne
    @JoinColumn(name = "id_zone", nullable = false)
    private ZoneEntity zone;

    /** Chargé par paquets quand on lit plusieurs lots à la fois, pour éviter une requête par lot. */
    @OneToMany(mappedBy = "lot", fetch = FetchType.LAZY)
    @BatchSize(size = 100)
    private List<HistoriqueEntity> historique = new ArrayList<>();
}
