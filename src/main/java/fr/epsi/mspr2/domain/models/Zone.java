package fr.epsi.mspr2.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(
    name = "zone",
    uniqueConstraints = @UniqueConstraint(name = "uk_zone_nom_entrepot", columnNames = {"nom", "id_entrepot"})
)
@Data
@NoArgsConstructor
public class Zone {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id_zone", updatable = false, nullable = false)
    private UUID idZone;

    @Column(name = "nom", length = 50, nullable = false)
    private String nom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_entrepot", nullable = false)
    @JsonIgnore
    private Entrepot entrepot;
}