package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.api.dto.MesureResponseDTO;
import fr.epsi.mspr2.api.mapper.MesureMapper;
import fr.epsi.mspr2.domain.services.CapteurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/capteurs")
public class CapteurController {

    private final CapteurService serviceCapteur;
    private final MesureMapper mesureMapper;

    @GetMapping("/{capteurId}/mesures")
    public ResponseEntity<List<MesureResponseDTO>> getMesuresByCapteurId(@PathVariable UUID capteurId) {
        try {
            return ResponseEntity.ok(serviceCapteur.getMesuresByCapteurId(capteurId).stream()
                    .map(mesureMapper::toResponse)
                    .toList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
