package fr.epsi.mspr2.infrastructure.adapters;

import fr.epsi.mspr2.domain.models.Capteur;
import fr.epsi.mspr2.domain.models.Mesure;
import fr.epsi.mspr2.infrastructure.jpa.JpaMesureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class MesureAdapterTest {

    private JpaMesureRepository jpaMesureRepository;
    private MesureAdapter adapter;

    @BeforeEach
    void setUp() {
        jpaMesureRepository = mock(JpaMesureRepository.class);
        adapter = new MesureAdapter(jpaMesureRepository);
    }

    private Mesure buildValidMesure() {
        Mesure m = new Mesure();
        Capteur capteur = new Capteur();
        capteur.setIdCapteur(UUID.randomUUID());
        m.setCapteur(capteur);
        m.setTemperature(new BigDecimal("20.0"));
        m.setHumidite(new BigDecimal("50.0"));
        return m;
    }

    @Test
    void save_shouldThrowWhenCapteurIsNull() {
        Mesure m = new Mesure();
        m.setCapteur(null);
        m.setTemperature(new BigDecimal("20.0"));
        m.setHumidite(new BigDecimal("50.0"));

        assertThatThrownBy(() -> adapter.save(m))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("capteur");
    }

    @Test
    void save_shouldThrowWhenTemperatureIsNull() {
        Mesure m = new Mesure();
        Capteur capteur = new Capteur();
        capteur.setIdCapteur(UUID.randomUUID());
        m.setCapteur(capteur);
        m.setTemperature(null);
        m.setHumidite(new BigDecimal("50.0"));

        assertThatThrownBy(() -> adapter.save(m))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("temperature");
    }

    @Test
    void save_shouldSaveWhenValid() {
        Mesure m = buildValidMesure();

        adapter.save(m);

        verify(jpaMesureRepository).save(m);
    }

    @Test
    void findById_shouldReturnMesureWhenFound() {
        UUID id = UUID.randomUUID();
        Mesure m = new Mesure();
        m.setIdMesure(id);
        when(jpaMesureRepository.findById(id)).thenReturn(Optional.of(m));

        Mesure result = adapter.findById(id);

        assertThat(result).isEqualTo(m);
    }

    @Test
    void findById_shouldReturnNullWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(jpaMesureRepository.findById(id)).thenReturn(Optional.empty());

        Mesure result = adapter.findById(id);

        assertThat(result).isNull();
    }

    @Test
    void delete_shouldCallRepositoryDeleteById() {
        UUID id = UUID.randomUUID();

        adapter.delete(id);

        verify(jpaMesureRepository).deleteById(id);
    }
}

