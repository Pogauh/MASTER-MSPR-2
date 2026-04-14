package fr.epsi.mspr2.domain.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historique")
@Data
@NoArgsConstructor
public class Historique {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id_historique", updatable = false, nullable = false)
    private UUID idHistorique;

    @Column(name = "type", length = 50)
    private String type;

    @Column(name = "date_emission")
    private LocalDateTime dateEmission;

    @Column(name = "zone_precedente", length = 50)
    private String zonePrecedente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_lot", nullable = false)
    private Lot lot;
}

