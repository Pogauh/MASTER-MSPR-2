package fr.epsi.mspr2.infrastructure.persistence.entity;

import fr.epsi.mspr2.domain.models.TypeAlerteEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alerte")
@Getter
@Setter
@NoArgsConstructor
public class AlerteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_alerte", updatable = false, nullable = false)
    private UUID idAlerte;

    @Column(name = "type_alerte", length = 50)
    @Enumerated(EnumType.STRING)
    private TypeAlerteEnum typeAlerte;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "date_emission")
    private LocalDateTime dateEmission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_capteur")
    private CapteurEntity capteur;
}
