package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.api.dto.HistoriqueResponseDto;
import fr.epsi.mspr2.api.dto.LotEmplacementRequestDTO;
import fr.epsi.mspr2.api.dto.LotRequestDTO;
import fr.epsi.mspr2.api.dto.LotResponseDTO;
import fr.epsi.mspr2.api.dto.LotStatusRequestDTO;
import fr.epsi.mspr2.api.dto.LotStatusResponseDTO;
import fr.epsi.mspr2.api.dto.MesureLotResponseDTO;
import fr.epsi.mspr2.api.mapper.LotMapper;
import fr.epsi.mspr2.api.mapper.MesureMapper;
import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.domain.services.LotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/lots")
public class LotController {

    private final LotService service;
    private final LotMapper lotMapper;
    private final MesureMapper mesureMapper;

    @PostMapping
    public ResponseEntity<LotStatusResponseDTO> createLot(@RequestBody LotRequestDTO request) {
        Lot lot = service.createLot(lotMapper.toLot(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new LotStatusResponseDTO(lot.getIdLot(), HttpStatus.CREATED.name()));
    }

    @GetMapping("/getLot/{idLot}")
    public ResponseEntity<LotResponseDTO> getLotById(@PathVariable("idLot") UUID idLot) {
        return service.getLotById(idLot)
                .map(lotMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/allLotsFromAllEntrepots")
    public List<LotResponseDTO> getAllLots() {
        return service.getAllLotsForAllEntrepot().stream()
                .map(lotMapper::toResponse)
                .toList();
    }

    @GetMapping("/getAllQuantity")
    public Integer getInventaireMondial() {
        return service.getQuantiteTotale();
    }

    @GetMapping("/getAllMesuresByLotId/{idLot}")
    public ResponseEntity<List<MesureLotResponseDTO>> getAllMesuresByLotId(@PathVariable("idLot") UUID idLot) {
        List<Mesure> mesures = service.getAllMesuresByLotId(idLot);
        if (mesures.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mesures.stream().map(mesureMapper::toLotResponse).toList());
    }

    @DeleteMapping("/deleteLot/{idLot}")
    public ResponseEntity<LotStatusResponseDTO> deleteLot(@PathVariable("idLot") UUID idLot) {
        return toStatusResponse(service.deleteLot(idLot));
    }

    @PutMapping("/{idLot}/zone")
    public ResponseEntity<LotStatusResponseDTO> updateEmplacementLot(@PathVariable("idLot") UUID idLot,
                                                                     @RequestBody LotEmplacementRequestDTO request) {
        return toStatusResponse(service.updateEmplacementLot(idLot, request.idZone));
    }

    @PutMapping("/{idLot}/status")
    public ResponseEntity<LotStatusResponseDTO> updateStatusLot(@PathVariable("idLot") UUID idLot,
                                                                @RequestBody LotStatusRequestDTO request) {
        return toStatusResponse(service.updateStatusLot(idLot, lotMapper.toStatus(request.status)));
    }

    @GetMapping("/oldestLot")
    public ResponseEntity<LotResponseDTO> getOldestLot() {
        return service.getOldestLot()
                .map(lotMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/stock-out")
    public ResponseEntity<HistoriqueResponseDto> stockOut(@RequestParam("lotId") UUID lotId) {
        Historique historique = service.stockOut(lotId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new HistoriqueResponseDto(historique.getIdHistorique(), HttpStatus.CREATED.name()));
    }

    /** 200 avec l'identifiant du lot s'il existe, 404 sinon. */
    private ResponseEntity<LotStatusResponseDTO> toStatusResponse(Optional<Lot> lot) {
        return lot
                .map(l -> ResponseEntity.ok(new LotStatusResponseDTO(l.getIdLot(), HttpStatus.OK.name())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new LotStatusResponseDTO(null, HttpStatus.NOT_FOUND.name())));
    }
}
