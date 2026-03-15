package fr.epsi.mspr2.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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

    @Column(name = "temp_ideale", precision = 5, scale = 2)
    private BigDecimal tempIdeale;

    @Column(name = "tolerance_temp", precision = 3, scale = 2)
    private BigDecimal toleranceTemp;

    @Column(name = "tolerance_humi", precision = 3, scale = 2)
    private BigDecimal toleranceHumi;

    @Column(name = "humi_ideale", precision = 5, scale = 2)
    private BigDecimal humiIdeale;

    @Column(name = "email_responsable", length = 100)
    private String emailResponsable;
}