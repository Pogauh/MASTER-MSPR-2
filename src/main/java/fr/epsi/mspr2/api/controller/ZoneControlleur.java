package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.domain.services.ZoneService;
import fr.epsi.mspr2.infrastructure.dto.MesureResponseDTO;
import fr.epsi.mspr2.infrastructure.mapper.ZoneMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/zone")
public class ZoneControlleur {

    private final ZoneService zoneService;
    private final ZoneMapper zoneMapper;

    @GetMapping("/readingsByDay/{zoneId}")
    public ResponseEntity<List<MesureResponseDTO>> getMesuresByZoneIdAndDayRange(
            @PathVariable UUID zoneId,
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate
    ) {
        try {
            List<MesureResponseDTO> dtos = zoneService.findMesuresByZoneIdAndDayRange(zoneId, startDate, endDate)
                    .orElse(List.of())
                    .stream()
                    .map(zoneMapper::toMesureResponseDto)
                    .toList();

            return ResponseEntity.ok(dtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
