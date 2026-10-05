package fr.epsi.mspr2.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mesure")
@Getter
@Setter
@NoArgsConstructor
public class MesureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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
    private CapteurEntity capteur;
}
