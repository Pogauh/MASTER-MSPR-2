package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.domain.services.AuthService;
import fr.epsi.mspr2.infrastructure.dto.auth.AuthResponseDTO;
import fr.epsi.mspr2.infrastructure.dto.auth.LoginRequestDTO;
import fr.epsi.mspr2.infrastructure.dto.auth.RegisterRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponseDTO register(@RequestBody RegisterRequestDTO request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }
}