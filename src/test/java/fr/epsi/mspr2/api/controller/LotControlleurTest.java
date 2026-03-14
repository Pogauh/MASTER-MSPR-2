package fr.epsi.mspr2.api.controller;

import fr.epsi.mspr2.adapters.dto.LotStatusResponseDTO;
import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.domain.services.ServiceLot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LotControlleurTest {
    private MockMvc mockMvc;
    private ServiceLot serviceLot;

    @BeforeEach
    void setUp() {
        serviceLot = mock(ServiceLot.class);
        LotControlleur controller = new LotControlleur(serviceLot);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createLot_shouldReturn201WithId() throws Exception {
        UUID createdId = UUID.randomUUID();
        LotStatusResponseDTO response = new LotStatusResponseDTO();
        response.idLot = createdId;
        when(serviceLot.createLot(any(Lot.class))).thenReturn(response);

        mockMvc.perform(post("/api/lots")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.idLot").value(createdId.toString()))
            .andExpect(jsonPath("$.status").value(HttpStatus.CREATED.name()));

        verify(serviceLot).createLot(any(Lot.class));
    }

    @Test
    void deleteLot_shouldReturn200WithId() throws Exception {
        UUID id = UUID.randomUUID();
        LotStatusResponseDTO response = new LotStatusResponseDTO();
        response.idLot = id;
        when(serviceLot.deleteLot(id)).thenReturn(response);

        mockMvc.perform(delete("/api/lots/deleteLot/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.idLot").value(id.toString()))
            .andExpect(jsonPath("$.status").value(HttpStatus.OK.name()));

        verify(serviceLot).deleteLot(id);
    }

    @Test
    void deleteLot_shouldReturn404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(serviceLot.deleteLot(id)).thenReturn(null);

        mockMvc.perform(delete("/api/lots/deleteLot/{id}", id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.idLot", nullValue()))
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.name()));

        verify(serviceLot).deleteLot(id);
    }
}

