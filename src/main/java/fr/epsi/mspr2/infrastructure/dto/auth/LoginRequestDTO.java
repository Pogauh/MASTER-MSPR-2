package fr.epsi.mspr2.infrastructure.dto.auth;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String email;
    private String password;
}