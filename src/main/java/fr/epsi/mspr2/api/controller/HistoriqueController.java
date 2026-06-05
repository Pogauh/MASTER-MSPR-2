package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.domain.models.Historique;
import fr.epsi.mspr2.domain.models.TypeHistoriqueEnum;
import fr.epsi.mspr2.domain.services.HistoriqueService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/historique")
public class HistoriqueController {

    private final HistoriqueService historiqueService;

    @GetMapping("/allHistoriquesForLot/{idLot}")
    public ResponseEntity<List<Historique>> getAllHistoriquesForLot(@PathVariable("idLot") UUID idLot,
                                                                    @RequestParam(value = "type", required = false) String type) {
        if (type == null || type.isBlank()) {
            return ResponseEntity.ok(historiqueService.findAllHistoriquesForLot(idLot));
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

        return ResponseEntity.ok(historiqueService.findAllHistoriquesForLotByType(idLot, enumType));
    }
}
