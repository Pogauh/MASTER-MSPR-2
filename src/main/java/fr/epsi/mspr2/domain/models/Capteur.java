package fr.epsi.mspr2.domain.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "capteur")
@Data
@NoArgsConstructor
public class Capteur {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id_capteur", updatable = false, nullable = false)
    private UUID idCapteur;

    @Column(name = "code_serie", length = 50, nullable = false, unique = true)
    private String codeSerie;

    @Column(name = "nom", length = 50)
    private String nom;

    @Column(name = "emplacement_precis", length = 150)
    private String emplacementPrecis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_entrepot", nullable = false)
    @JsonIgnore
    private Entrepot entrepot;
}

