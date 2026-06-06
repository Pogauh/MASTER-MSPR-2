package fr.epsi.mspr2.api.controller;


import fr.epsi.mspr2.domain.models.Lot;
import fr.epsi.mspr2.infrastructure.dto.LotStatusResponseDTO;
import fr.epsi.mspr2.domain.services.LotService;
import fr.epsi.mspr2.infrastructure.mapper.LotMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import fr.epsi.mspr2.infrastructure.dto.LotResponseDTO;

import java.util.List;

class LotControlleurTest {
    private MockMvc mockMvc;
    private LotService serviceLot;
    private LotMapper lotMapper;


    @BeforeEach
    void setUp() {
        serviceLot = mock(LotService.class);
        lotMapper = mock(LotMapper.class);
        LotControlleur controller = new LotControlleur(serviceLot, lotMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createLot_shouldReturn201WithId() throws Exception {
        UUID createdId = UUID.randomUUID();
        Lot response = new Lot();
        response.setIdLot(createdId);
        
        LotStatusResponseDTO statusResponse = new LotStatusResponseDTO(createdId, null);
        
        when(lotMapper.lotToEntity(any())).thenReturn(new Lot());
        when(serviceLot.createLot(any(Lot.class))).thenReturn(response);
        when(lotMapper.toStatusResponse(response)).thenReturn(statusResponse);

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
        Lot response = new Lot();
        response.setIdLot(id);
        
        LotStatusResponseDTO statusResponse = new LotStatusResponseDTO(id, null);
        
        when(serviceLot.deleteLot(id)).thenReturn(response);
        when(lotMapper.toStatusResponse(response)).thenReturn(statusResponse);

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

    @Test
    void getLotById_shouldReturn200WhenFound() throws Exception {
        UUID id = UUID.randomUUID();
        Lot lot = new Lot();
        lot.setIdLot(id);
        
        LotResponseDTO responseDTO = new LotResponseDTO();
        responseDTO.idLot = id;
        
        when(serviceLot.getLotById(id)).thenReturn(lot);
        when(lotMapper.toResponse(lot)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/lots/getLot/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.idLot").value(id.toString()));

        verify(serviceLot).getLotById(id);
    }

    @Test
    void getLotById_shouldReturn404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        
        when(serviceLot.getLotById(id)).thenReturn(null);

        mockMvc.perform(get("/api/lots/getLot/{id}", id))
            .andExpect(status().isNotFound());

        verify(serviceLot).getLotById(id);
    }

    @Test
    void getAllLots_shouldReturnListOfLots() throws Exception {
        Lot lot = new Lot();
        UUID id = UUID.randomUUID();
        lot.setIdLot(id);
        
        LotResponseDTO responseDTO = new LotResponseDTO();
        responseDTO.idLot = id;

        when(serviceLot.getAllLotsForAllEntrepot()).thenReturn(List.of(lot));
        when(lotMapper.toResponse(lot)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/lots/allLotsFromAllEntrepots"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].idLot").value(id.toString()));

        verify(serviceLot).getAllLotsForAllEntrepot();
    }

    @Test
    void getAllLots_shouldReturnEmptyListWhenNull() throws Exception {
        when(serviceLot.getAllLotsForAllEntrepot()).thenReturn(null);

        mockMvc.perform(get("/api/lots/allLotsFromAllEntrepots"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());

        verify(serviceLot).getAllLotsForAllEntrepot();
    }

    @Test
    void getInventaireMondial_shouldReturnQuantity() throws Exception {
        when(serviceLot.getQuantiteTotale()).thenReturn(150);

        mockMvc.perform(get("/api/lots/getAllQuantity"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(150));

        verify(serviceLot).getQuantiteTotale();
    }

    @Test
    void updateLot_shouldReturn200WithId() throws Exception {
        UUID id = UUID.randomUUID();
        Lot lot = new Lot();
        lot.setIdLot(id);
        
        LotStatusResponseDTO statusResponse = new LotStatusResponseDTO(id, null);
        
        when(lotMapper.lotEmplacementToEntity(any())).thenReturn(lot);
        when(serviceLot.updateEmplacementLot(any(UUID.class), any(Lot.class))).thenReturn(lot);
        when(lotMapper.toStatusResponse(lot)).thenReturn(statusResponse);

        mockMvc.perform(put("/api/lots/emplacementLot/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.idLot").value(id.toString()))
            .andExpect(jsonPath("$.status").value(HttpStatus.OK.name()));

        verify(serviceLot).updateEmplacementLot(any(UUID.class), any(Lot.class));
    }

    @Test
    void updateLot_shouldReturn404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        
        when(lotMapper.lotEmplacementToEntity(any())).thenReturn(new Lot());
        when(serviceLot.updateEmplacementLot(any(UUID.class), any(Lot.class))).thenReturn(null);

        mockMvc.perform(put("/api/lots/emplacementLot/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.idLot", nullValue()))
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.name()));

        verify(serviceLot).updateEmplacementLot(any(UUID.class), any(Lot.class));
    }
}
