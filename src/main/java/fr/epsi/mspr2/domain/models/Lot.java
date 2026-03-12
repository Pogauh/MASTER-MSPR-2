package fr.epsi.mspr2.domain.models;

import lombok.Data;

@Data
public class Lot {
    private String id_lot;
    private String status;
    private Integer quantite;
    private String caracteristque;
}
