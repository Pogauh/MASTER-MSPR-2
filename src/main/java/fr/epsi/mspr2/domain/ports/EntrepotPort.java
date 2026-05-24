package fr.epsi.mspr2.domain.ports;

import fr.epsi.mspr2.domain.models.Entrepot;

import java.util.List;

public interface EntrepotPort {
    List<Entrepot> findAll();
}

