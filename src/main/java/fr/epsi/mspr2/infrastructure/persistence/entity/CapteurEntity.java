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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "capteur")
@Getter
@Setter
@NoArgsConstructor
public class CapteurEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_capteur", updatable = false, nullable = false)
    private UUID idCapteur;

    @Column(name = "code_serie", length = 50, nullable = false, unique = true)
    private String codeSerie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_zone", nullable = false, unique = true)
    private ZoneEntity zone;
}
