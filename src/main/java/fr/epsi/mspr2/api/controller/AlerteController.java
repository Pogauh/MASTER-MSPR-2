package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.api.dto.AlerteDetailsResponseDto;
import fr.epsi.mspr2.api.mapper.AlerteMapper;
import fr.epsi.mspr2.domain.services.AlerteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class AlerteController {

    private final AlerteService service;
    private final AlerteMapper mapper;

    @GetMapping("/alertes")
    public ResponseEntity<List<AlerteDetailsResponseDto>> getAlerte(
            @RequestParam(value = "idEntrepot") UUID idEntrepot,
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        try {
            List<AlerteDetailsResponseDto> response = service.getAlerteByEntrepot(idEntrepot, startDate, endDate)
                    .stream()
                    .map(mapper::toDto)
                    .toList();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
