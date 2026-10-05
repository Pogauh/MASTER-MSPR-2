package fr.epsi.mspr2.domain.exceptions;

import java.util.UUID;

/** Le lot demandé n'existe pas. */
public class LotIntrouvableException extends RuntimeException {

    public LotIntrouvableException(UUID idLot) {
        super("Lot introuvable : " + idLot);
    }
}
