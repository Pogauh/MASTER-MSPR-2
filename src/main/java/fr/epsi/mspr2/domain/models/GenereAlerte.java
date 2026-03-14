package fr.epsi.mspr2.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "genere_alerte")
@Data
@NoArgsConstructor
public class GenereAlerte {
    @Id
    @ManyToOne
    @JoinColumn(name = "id_lot", nullable = false)
    private Lot lot;

    @Id
    @ManyToOne
    @JoinColumn(name = "id_alerte", nullable = false)
    private Alerte alerte;
}