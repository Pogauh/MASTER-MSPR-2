
package fr.epsi.mspr2.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mesure")
@Data
@NoArgsConstructor
public class Mesure {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id_mesure", updatable = false, nullable = false)
    private UUID idMesure;

    @Column(name = "temperature", precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(name = "humidite", precision = 5, scale = 2)
    private BigDecimal humidite;

    @Column(name = "date_mesure")
    private LocalDateTime dateMesure;

    @ManyToOne
    @JoinColumn(name = "id_capteur", nullable = false)
    @JsonIgnore
    private Capteur capteur;

    @PrePersist
    public void applyDefaults() {
        if (dateMesure == null) {
            dateMesure = LocalDateTime.now();
        }
    }
}