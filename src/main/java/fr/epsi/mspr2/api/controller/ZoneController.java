package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.api.dto.MesureResponseDTO;
import fr.epsi.mspr2.api.mapper.MesureMapper;
import fr.epsi.mspr2.domain.services.ZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/zone")
public class ZoneController {

    private final ZoneService zoneService;
    private final MesureMapper mesureMapper;

    // DATE Attendu ex : 2024-06-05T18:46
    @GetMapping("/readings/{zoneId}")
    public ResponseEntity<List<MesureResponseDTO>> getMesuresByZoneId(
            @PathVariable UUID zoneId,

            @RequestParam("startDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @RequestParam("endDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        try {
            List<MesureResponseDTO> dtos = zoneService.findMesuresByZoneId(zoneId, startDate, endDate)
                    .stream()
                    .map(mesureMapper::toResponse)
                    .toList();

            return ResponseEntity.ok(dtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{zoneId}/readings/latest")
    public ResponseEntity<MesureResponseDTO> getLastMesuresByZoneId(@PathVariable UUID zoneId) {
        return zoneService.findLastMesuresByZoneId(zoneId)
                .map(mesureMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
