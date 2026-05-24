package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.domain.models.Entrepot;
import fr.epsi.mspr2.domain.services.EntrepotService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/entrepots")
public class EntrepotController {
    private final EntrepotService service;

    @GetMapping
    public List<Entrepot> getAllEntrepots() {
        return service.getAllEntrepots();
    }
}

