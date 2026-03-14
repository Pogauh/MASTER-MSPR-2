
package fr.epsi.mspr2.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lot")
@Data
@NoArgsConstructor
public class Lot {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id_lot", updatable = false, nullable = false)
    private UUID idLot;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "quantite")
    private Integer quantite;

    @Column(name = "caracteristique")
    private Integer caracteristique;

    @Column(name = "date_sortie")
    private LocalDateTime dateSortie;

    @Column(name = "date_stockage")
    private LocalDateTime dateStockage;

    @ManyToOne
    @JoinColumn(name = "id_exploitation", nullable = false)
    private Exploitation exploitation;

    @ManyToOne
    @JoinColumn(name = "id_entrepot", nullable = false)
    @JsonIgnore
    private Entrepot entrepot;
}