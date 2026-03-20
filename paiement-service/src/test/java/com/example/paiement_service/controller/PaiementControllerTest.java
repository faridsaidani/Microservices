package com.example.paiement_service.controller;

import com.example.paiement_service.dto.PaiementRequestDTO;
import com.example.paiement_service.dto.PaiementResponseDTO;
import com.example.paiement_service.exception.ResourceNotFoundException;
import com.example.paiement_service.service.PaiementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaiementController.class)
class PaiementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaiementService paiementService;

    @Autowired
    private ObjectMapper objectMapper;

    private PaiementResponseDTO buildPaiementResponse(Long id) {
        PaiementResponseDTO dto = new PaiementResponseDTO();
        dto.setId(id);
        dto.setCommandeId(10L);
        dto.setMontant(1999.98);
        dto.setModePaiement("CARTE_BANCAIRE");
        dto.setStatut("ACCEPTE");
        dto.setDatePaiement(LocalDateTime.of(2024, 1, 15, 10, 0));
        return dto;
    }

    // ──────────────────────────────────────────────────────────────
    // GET /api/v1/paiements
    // ──────────────────────────────────────────────────────────────

    @Test
    void getAllPaiements_shouldReturn200WithList() throws Exception {
        when(paiementService.getAllPaiements()).thenReturn(List.of(buildPaiementResponse(1L)));

        mockMvc.perform(get("/api/v1/paiements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].modePaiement").value("CARTE_BANCAIRE"))
                .andExpect(jsonPath("$[0].statut").value("ACCEPTE"));
    }

    @Test
    void getAllPaiements_shouldReturn200WithEmptyList() throws Exception {
        when(paiementService.getAllPaiements()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/paiements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ──────────────────────────────────────────────────────────────
    // GET /api/v1/paiements/{id}
    // ──────────────────────────────────────────────────────────────

    @Test
    void getPaiementById_shouldReturn200WhenFound() throws Exception {
        when(paiementService.getPaiementById(1L)).thenReturn(buildPaiementResponse(1L));

        mockMvc.perform(get("/api/v1/paiements/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.commandeId").value(10))
                .andExpect(jsonPath("$.montant").value(1999.98));
    }

    @Test
    void getPaiementById_shouldReturn404WhenNotFound() throws Exception {
        when(paiementService.getPaiementById(99L))
                .thenThrow(new ResourceNotFoundException("Paiement non trouvé avec id: 99"));

        mockMvc.perform(get("/api/v1/paiements/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ──────────────────────────────────────────────────────────────
    // GET /api/v1/paiements/commande/{commandeId}
    // ──────────────────────────────────────────────────────────────

    @Test
    void getPaiementsByCommandeId_shouldReturn200WithList() throws Exception {
        when(paiementService.getPaiementsByCommandeId(10L))
                .thenReturn(List.of(buildPaiementResponse(1L)));

        mockMvc.perform(get("/api/v1/paiements/commande/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].commandeId").value(10))
                .andExpect(jsonPath("$[0].statut").value("ACCEPTE"));
    }

    @Test
    void getPaiementsByCommandeId_shouldReturn200WithEmptyList() throws Exception {
        when(paiementService.getPaiementsByCommandeId(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/paiements/commande/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ──────────────────────────────────────────────────────────────
    // POST /api/v1/paiements
    // ──────────────────────────────────────────────────────────────

    @Test
    void createPaiement_shouldReturn201WhenValid() throws Exception {
        PaiementRequestDTO req = new PaiementRequestDTO(10L, "CARTE_BANCAIRE");
        PaiementResponseDTO res = buildPaiementResponse(1L);
        when(paiementService.createPaiement(any(PaiementRequestDTO.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/paiements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.modePaiement").value("CARTE_BANCAIRE"))
                .andExpect(jsonPath("$.statut").value("ACCEPTE"));
    }

    @Test
    void createPaiement_shouldReturn400WhenCommandeIdIsNull() throws Exception {
        PaiementRequestDTO invalid = new PaiementRequestDTO(null, "CARTE_BANCAIRE");

        mockMvc.perform(post("/api/v1/paiements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.commandeId").exists());
    }

    @Test
    void createPaiement_shouldReturn400WhenModePaiementIsNull() throws Exception {
        PaiementRequestDTO invalid = new PaiementRequestDTO(10L, null);

        mockMvc.perform(post("/api/v1/paiements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.modePaiement").exists());
    }

    // ──────────────────────────────────────────────────────────────
    // DELETE /api/v1/paiements/{id}
    // ──────────────────────────────────────────────────────────────

    @Test
    void deletePaiement_shouldReturn204WhenFound() throws Exception {
        doNothing().when(paiementService).deletePaiement(1L);

        mockMvc.perform(delete("/api/v1/paiements/1"))
                .andExpect(status().isNoContent());

        verify(paiementService, times(1)).deletePaiement(1L);
    }

    @Test
    void deletePaiement_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Paiement non trouvé avec id: 99"))
                .when(paiementService).deletePaiement(99L);

        mockMvc.perform(delete("/api/v1/paiements/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
