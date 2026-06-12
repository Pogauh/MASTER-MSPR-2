package fr.epsi.mspr2.infrastructure.jpa;

import fr.epsi.mspr2.domain.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

}