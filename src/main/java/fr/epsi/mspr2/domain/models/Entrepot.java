package fr.epsi.mspr2.domain.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "entrepot")
@Data
@NoArgsConstructor
public class Entrepot {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id_entrepot")
    private UUID idEntrepot;

    @Column(name = "localisation", length = 150)
    private String localisation;

    @Column(name = "nom", length = 50)
    private String nom;
}