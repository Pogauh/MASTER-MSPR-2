package fr.epsi.mspr2.infrastructure.dto.auth;

import fr.epsi.mspr2.domain.models.Role;
import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String email;
    private String password;
    private Role role;
}