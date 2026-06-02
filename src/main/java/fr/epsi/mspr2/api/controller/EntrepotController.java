package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.domain.models.Entrepot;
import fr.epsi.mspr2.domain.services.EntrepotService;
import fr.epsi.mspr2.infrastructure.dto.CapteurSummaryResponseDTO;
import fr.epsi.mspr2.infrastructure.mapper.CapteurMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/entrepots")
public class EntrepotController {
    private final EntrepotService service;
    private final CapteurMapper capteurMapper;

    @GetMapping
    public List<Entrepot> getAllEntrepots() {
        return service.getAllEntrepots();
    }

    @GetMapping(value = {"/{entrepotId}/capteurSummary"})
    public ResponseEntity<List<CapteurSummaryResponseDTO>> getCapteurSummary(@PathVariable UUID entrepotId) {
        try {
            List<CapteurSummaryResponseDTO> response = service.getCapteurSummaryByEntrepotId(entrepotId)
                    .stream()
                    .map(capteurMapper::toResponse)
                    .toList();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
