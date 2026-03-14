package fr.epsi.mspr2.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "alerte")
@Data
@NoArgsConstructor
public class Alerte {
    @Id
    @GeneratedValue(generator = "UUID")
    @org.hibernate.annotations.GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id_alerte", updatable = false, nullable = false)
    private UUID idAlerte;

    @Column(name = "type_alerte", length = 50)
    private String typeAlerte;

    @Column(name = "description", length = 50)
    private String description;

    @Column(name = "date_emission")
    private LocalDateTime dateEmission;
}