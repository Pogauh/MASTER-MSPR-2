package fr.epsi.mspr2.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EntrepotOwnerProperties {
    private final UUID entrepotId;

    public EntrepotOwnerProperties(@Value("${app.entrepot.id}") String entrepotId) {
        this.entrepotId = UUID.fromString(entrepotId);
    }

    public UUID getEntrepotId() {
        return entrepotId;
    }
}

