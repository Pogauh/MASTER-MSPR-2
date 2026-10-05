package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.api.dto.HistoriqueDto;
import fr.epsi.mspr2.api.mapper.HistoriqueMapper;
import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.domain.services.HistoriqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/historique")
public class HistoriqueController {

    private final HistoriqueService historiqueService;
    private final HistoriqueMapper historiqueMapper;

    @GetMapping("/allHistoriquesForLot/{idLot}")
    public ResponseEntity<List<HistoriqueDto>> getAllHistoriquesForLot(@PathVariable("idLot") UUID idLot,
                                                                       @RequestParam(value = "type", required = false) String type) {
        if (type == null || type.isBlank()) {
            return ResponseEntity.ok(toDtos(historiqueService.findAllHistoriquesForLot(idLot)));
        }

        String normalized = type.trim().toUpperCase();
        if ("ALERT".equals(normalized)) {
            normalized = "ALERTE";
        }

        TypeHistoriqueEnum enumType;
        try {
            enumType = TypeHistoriqueEnum.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(toDtos(historiqueService.findAllHistoriquesForLotByType(idLot, enumType)));
    }

    private List<HistoriqueDto> toDtos(List<Historique> historiques) {
        return historiques.stream().map(historiqueMapper::toDto).toList();
    }
}
