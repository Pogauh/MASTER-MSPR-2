package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.api.dto.MesureRequestDTO;
import fr.epsi.mspr2.api.dto.MesureResponseDTO;
import fr.epsi.mspr2.api.dto.MesureStatusResponseDTO;
import fr.epsi.mspr2.api.mapper.MesureMapper;
import fr.epsi.mspr2.domain.services.MesureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/mesures")
public class MesureController {

    private final MesureService serviceMesure;
    private final MesureMapper mesureMapper;

    @PostMapping
    public ResponseEntity<MesureStatusResponseDTO> createMesure(@RequestBody MesureRequestDTO dto) {
        serviceMesure.createMesure(mesureMapper.toNouvelleMesure(dto));
        // Comportement historique de l'API : l'identifiant de la mesure créée n'est pas renvoyé (idMesure = null).
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MesureStatusResponseDTO(null, HttpStatus.CREATED.name()));
    }

    /** Renvoie un corps vide (200) quand la mesure n'existe pas. */
    @GetMapping("/getMesure/{id}")
    public MesureResponseDTO getMesureById(@PathVariable("id") UUID idMesure) {
        return serviceMesure.getMesureById(idMesure)
                .map(mesureMapper::toResponse)
                .orElse(null);
    }

    @DeleteMapping("/deleteMesure/{id}")
    public ResponseEntity<MesureStatusResponseDTO> deleteMesure(@PathVariable("id") UUID idMesure) {
        return serviceMesure.deleteMesure(idMesure)
                .map(mesure -> ResponseEntity.ok(new MesureStatusResponseDTO(mesure.getIdMesure(), HttpStatus.OK.name())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MesureStatusResponseDTO(null, HttpStatus.NOT_FOUND.name())));
    }
}
