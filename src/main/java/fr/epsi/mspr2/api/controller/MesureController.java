package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.infrastructure.dto.MesureRequestDTO;
import fr.epsi.mspr2.infrastructure.dto.MesureResponseDTO;
import fr.epsi.mspr2.infrastructure.dto.MesureStatusResponseDTO;
import fr.epsi.mspr2.domain.services.MesureService;
import fr.epsi.mspr2.infrastructure.mapper.MesureMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/mesures")
public class MesureController {

    private final MesureService serviceMesure;
    private final MesureMapper mesureMapper;

    @PostMapping
    public ResponseEntity<MesureStatusResponseDTO> createMesure(@RequestBody MesureRequestDTO dto) {
        MesureStatusResponseDTO response = serviceMesure.createMesure(mesureMapper.mesureRequestDtoToMesureRequest(dto));
        response.status = HttpStatus.CREATED.name();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/getMesure/{id}")
    public MesureResponseDTO getMesureById(@PathVariable("id") UUID idMesure) {
        return serviceMesure.getMesureById(idMesure);
    }


    @DeleteMapping("/deleteMesure/{id}")
    public ResponseEntity<MesureStatusResponseDTO> deleteMesure(@PathVariable("id") UUID idMesure) {
        MesureStatusResponseDTO response = serviceMesure.deleteMesure(idMesure);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MesureStatusResponseDTO(null, HttpStatus.NOT_FOUND.name()));
        }
        response.status = HttpStatus.OK.name();
        return ResponseEntity.ok(response);
    }
}