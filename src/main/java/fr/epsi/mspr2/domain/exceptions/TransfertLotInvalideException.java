package fr.epsi.mspr2.domain.exceptions;

/** Le transfert demandé pour un lot n'est pas possible (zone de destination absente ou identique à la zone actuelle). */
public class TransfertLotInvalideException extends RuntimeException {

    public TransfertLotInvalideException(String message) {
        super(message);
    }
}
