package fr.epsi.mspr2.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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
    @JsonIgnore
    private Lot lot;
}

