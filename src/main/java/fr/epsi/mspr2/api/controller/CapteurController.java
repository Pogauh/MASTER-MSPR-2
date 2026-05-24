package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.infrastructure.dto.MesureResponseDTO;
import fr.epsi.mspr2.domain.services.CapteurService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/capteurs")
public class CapteurController {

    private final CapteurService serviceCapteur;

    @GetMapping("/{capteurId}/mesures")
    public ResponseEntity<List<MesureResponseDTO>> getMesuresByCapteurId(@PathVariable UUID capteurId) {
        try {
            return ResponseEntity.ok(serviceCapteur.getMesuresByCapteurId(capteurId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{capteurId}/mesures/by-day")
    public ResponseEntity<List<MesureResponseDTO>> getMesuresByCapteurIdAndDayRange(
        @PathVariable UUID capteurId,
        @RequestParam("startDate") LocalDate startDate,
        @RequestParam("endDate") LocalDate endDate
    ) {
        try {
            return ResponseEntity.ok(serviceCapteur.getMesuresByCapteurIdAndDayRange(capteurId, startDate, endDate));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{capteurId}/mesures/by-hour")
    public ResponseEntity<List<MesureResponseDTO>> getMesuresByCapteurIdAndHourRange(
        @PathVariable UUID capteurId,
        @RequestParam("startTime") LocalTime startTime,
        @RequestParam("endTime") LocalTime endTime
    ) {
        try {
            return ResponseEntity.ok(serviceCapteur.getMesuresByCapteurIdAndHourRange(capteurId, startTime, endTime));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
