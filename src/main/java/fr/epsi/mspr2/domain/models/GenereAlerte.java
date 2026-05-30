package fr.epsi.mspr2.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "genere_alerte")
@Data
@NoArgsConstructor
public class GenereAlerte {
    @EmbeddedId
    private GenereAlerteId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("lotId")
    @JoinColumn(name = "id_lot", nullable = false)
    private Lot lot;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("alerteId")
    @JoinColumn(name = "id_alerte", nullable = false)
    private Alerte alerte;

    public GenereAlerte(Lot lot, Alerte alerte) {
        this.lot = lot;
        this.alerte = alerte;
        this.id = new GenereAlerteId(lot.getIdLot(), alerte.getIdAlerte());
    }
}