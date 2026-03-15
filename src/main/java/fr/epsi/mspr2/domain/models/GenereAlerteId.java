package fr.epsi.mspr2.domain.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GenereAlerteId implements Serializable {
    @Column(name = "id_lot", nullable = false)
    private UUID lotId;

    @Column(name = "id_alerte", nullable = false)
    private UUID alerteId;
}
