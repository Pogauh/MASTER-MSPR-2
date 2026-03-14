package fr.epsi.mspr2.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "pays")
@Data
@NoArgsConstructor
public class Pays {
    @Id
    @GeneratedValue(generator = "UUID")
    @org.hibernate.annotations.GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id_pays", updatable = false, nullable = false)
    private UUID idPays;

    @Column(name = "nom", length = 50)
    private String nom;

    @Column(name = "temp_ideale")
    private Double tempIdeale;

    @Column(name = "tolerance_temp")
    private Double toleranceTemp;

    @Column(name = "tolerance_humi")
    private Double toleranceHumi;

    @Column(name = "humi_ideale")
    private Double humiIdeale;

    @Column(name = "email_responsable", length = 50)
    private String emailResponsable;
}