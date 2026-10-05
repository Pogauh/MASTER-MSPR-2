package fr.epsi.mspr2.api.exception;

import fr.epsi.mspr2.domain.exceptions.TransfertLotInvalideException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

/** Traduit en réponses HTTP les erreurs métier du domaine. */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** 400, avec le message de l'erreur dans le corps d'erreur standard de Spring Boot. */
    @ExceptionHandler(TransfertLotInvalideException.class)
    public void handleTransfertInvalide(TransfertLotInvalideException e, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }
}
