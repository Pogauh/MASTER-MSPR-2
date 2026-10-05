package fr.epsi.mspr2.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "zone",
        uniqueConstraints = @UniqueConstraint(name = "uk_zone_nom_entrepot", columnNames = {"nom", "id_entrepot"})
)
@Getter
@Setter
@NoArgsConstructor
public class ZoneEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_zone", updatable = false, nullable = false)
    private UUID idZone;

    @Column(name = "nom", length = 50, nullable = false)
    private String nom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_entrepot", nullable = false)
    private EntrepotEntity entrepot;
}
