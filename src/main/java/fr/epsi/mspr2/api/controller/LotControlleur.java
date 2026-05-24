package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.infrastructure.dto.LotResponseDTO;
import fr.epsi.mspr2.infrastructure.dto.LotRequestDTO;
import fr.epsi.mspr2.infrastructure.dto.LotStatusResponseDTO;
import fr.epsi.mspr2.domain.services.LotService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/lots")
public class LotControlleur {

    private final LotService service;

    @PostMapping
    public ResponseEntity<LotStatusResponseDTO> createLot(@RequestBody LotRequestDTO request) {
        LotStatusResponseDTO response = service.createLot(request);
        response.status = HttpStatus.CREATED.name();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/getLot/{id}")
    public LotResponseDTO getLotById(@PathVariable("id") UUID idLot) {
        return service.getLotById(idLot);
    }

    @GetMapping("/allLotsFromAllEntrepots")
    public List<LotResponseDTO> getAllLots() {
        return service.getAllLotsForAllEntrepot();
    }

    @GetMapping("/getAllQuantity")
    public Integer getInventaireMondial() {
        return service.getQuantiteTotale();
    }

    @DeleteMapping("/deleteLot/{id}")
    public ResponseEntity<LotStatusResponseDTO> deleteLot(@PathVariable("id") UUID idLot) {
        LotStatusResponseDTO response = service.deleteLot(idLot);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new LotStatusResponseDTO(null, HttpStatus.NOT_FOUND.name()));
        }
        response.status = HttpStatus.OK.name();
        return ResponseEntity.ok(response);
    }
}