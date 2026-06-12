package fr.epsi.mspr2.domain.services;

import fr.epsi.mspr2.domain.models.Role;
import fr.epsi.mspr2.domain.models.User;
import fr.epsi.mspr2.infrastructure.dto.auth.AuthResponseDTO;
import fr.epsi.mspr2.infrastructure.dto.auth.LoginRequestDTO;
import fr.epsi.mspr2.infrastructure.dto.auth.RegisterRequestDTO;
import fr.epsi.mspr2.infrastructure.jpa.JpaUserRepository;
import fr.epsi.mspr2.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JpaUserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }

        Role role = request.getRole() != null ? request.getRole() : Role.USER_SIEGE;

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponseDTO(token);
    }

    public AuthResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponseDTO(token);
    }
}