package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.api.dto.CapteurSummaryResponseDTO;
import fr.epsi.mspr2.api.dto.EntrepotDto;
import fr.epsi.mspr2.api.dto.ZoneResponseDto;
import fr.epsi.mspr2.api.mapper.EntrepotMapper;
import fr.epsi.mspr2.domain.services.EntrepotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/entrepots")
public class EntrepotController {

    private final EntrepotService service;
    private final EntrepotMapper mapper;

    @GetMapping
    public List<EntrepotDto> getAllEntrepots() {
        return service.getAllEntrepots().stream()
                .map(mapper::toDto)
                .toList();
    }

    @GetMapping("/{entrepotId}/capteurSummary")
    public ResponseEntity<List<CapteurSummaryResponseDTO>> getCapteurSummary(@PathVariable UUID entrepotId) {
        try {
            List<CapteurSummaryResponseDTO> response = service.getCapteurSummaryByEntrepotId(entrepotId)
                    .stream()
                    .map(mapper::toDto)
                    .toList();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{idEntrepot}/zones")
    public ResponseEntity<List<ZoneResponseDto>> getZonesByEntrepotId(@PathVariable UUID idEntrepot) {
        try {
            List<ZoneResponseDto> response = service.findZonesByEntrepotId(idEntrepot)
                    .stream()
                    .map(mapper::toDto)
                    .toList();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
