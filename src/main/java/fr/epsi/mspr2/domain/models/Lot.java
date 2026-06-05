package fr.epsi.mspr2.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "lot")
@Data
@NoArgsConstructor
public class Lot {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID" , strategy = "org.hibernate.id.UUIDGenerator")
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
    private Exploitation exploitation;

    @ManyToOne
    @JoinColumn(name = "id_zone", nullable = false)
    @JsonIgnore
    private Zone zone;

    @OneToMany(mappedBy = "lot", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Historique> historique = new ArrayList<>();

    @PrePersist
    public void applyDefaults() {
        if (status == null ) {
            status = StatusEnum.COMPLIANT;
        }
        if (dateStockage == null) {
            dateStockage = LocalDateTime.now();
        }
    }

    @Transient
    public Entrepot getEntrepot() {
        return zone != null ? zone.getEntrepot() : null;
    }

    public void setEntrepot(Entrepot entrepot) {
        if (zone == null) {
            zone = new Zone();
        }
        zone.setEntrepot(entrepot);
    }

    public enum StatusEnum {
        COMPLIANT, ALERT, BLOCKED, SHIPPED
    }
}