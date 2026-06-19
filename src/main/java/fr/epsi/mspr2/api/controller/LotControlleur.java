package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.infrastructure.dto.*;
import fr.epsi.mspr2.domain.services.LotService;
import fr.epsi.mspr2.infrastructure.mapper.HistoriqueMapper;
import fr.epsi.mspr2.infrastructure.mapper.LotMapper;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.infrastructure.mapper.MesureMapper;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/lots")
public class LotControlleur {

    private final LotService service;
    private final LotMapper lotMapper;
    private final HistoriqueMapper historiqueMapper;
    private final MesureMapper mesureMapper;


    @PostMapping
    public ResponseEntity<LotStatusResponseDTO> createLot(@RequestBody LotRequestDTO request) {

        LotStatusResponseDTO response = lotMapper.toStatusResponse(service.createLot(lotMapper.lotToEntity(request)));
        response.status = HttpStatus.CREATED.name();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/getLot/{idLot}")
    public ResponseEntity<LotResponseDTO> getLotById(@PathVariable("idLot") UUID idLot) {
        Lot lot = service.getLotById(idLot);
        if (lot == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(lotMapper.toResponse(lot));
    }

    @GetMapping("/allLotsFromAllEntrepots")
    public List<LotResponseDTO> getAllLots() {
        List<Lot> lots = service.getAllLotsForAllEntrepot();
        return lots != null ? lots.stream()
                .map(lotMapper::toResponse)
                .toList() : List.of();
    }

    @GetMapping("/getAllQuantity")
    public Integer getInventaireMondial() {
        return service.getQuantiteTotale();
    }

    @GetMapping("/getAllMesuresByLotId/{idLot}")
    public ResponseEntity<List<MesureLotResponseDTO>> getAllMesuresByLotId(@PathVariable("idLot") UUID idLot) {
        Optional<List<Mesure>> mesures = service.getAllMesuresByLotId(idLot);
        if (mesures.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<MesureLotResponseDTO> dtos = mesures.get().stream()
                .map(mesureMapper::toDtoforLot)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/deleteLot/{idLot}")
    public ResponseEntity<LotStatusResponseDTO> deleteLot(@PathVariable("idLot") UUID idLot) {
        Lot lot = service.deleteLot(idLot);
        if (lot == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new LotStatusResponseDTO(null, HttpStatus.NOT_FOUND.name()));
        }
        LotStatusResponseDTO response = lotMapper.toStatusResponse(lot);
        response.status = HttpStatus.OK.name();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{idLot}/zone")
    public ResponseEntity<LotStatusResponseDTO> updateEmplacementLot(@PathVariable("idLot") UUID idLot, @RequestBody LotEmplacementRequestDTO request) {
        Lot lot = service.updateEmplacementLot(idLot, lotMapper.lotEmplacementToEntity(request));
        if (lot == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new LotStatusResponseDTO(null, HttpStatus.NOT_FOUND.name()));
        }
        LotStatusResponseDTO response = lotMapper.toStatusResponse(lot);
        response.status = HttpStatus.OK.name();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{idLot}/status")
    public ResponseEntity<LotStatusResponseDTO> updateStatusLot(@PathVariable("idLot") UUID idLot, @RequestBody LotStatusRequestDTO request) {
        Lot lot = service.updateStatusLot(idLot, lotMapper.lotStatusToEntity(request));
        if (lot == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new LotStatusResponseDTO(null, HttpStatus.NOT_FOUND.name()));
        }
        LotStatusResponseDTO response = lotMapper.toStatusResponse(lot);
        response.status = HttpStatus.OK.name();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/oldestLot")
    public ResponseEntity<LotResponseDTO> getOldestLot(){
        Lot lot = service.getOldestLot();
        if (lot == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(lotMapper.toResponse(lot));
    }

    @PostMapping("/stock-out")
    public ResponseEntity<HistoriqueResponseDto> stockOut(@RequestParam("lotId") UUID lotId) {
        HistoriqueResponseDto response = historiqueMapper.toHistoriqueResponseDto(service.stockOut(lotId));
        response.status = HttpStatus.CREATED.name();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}