package com.example.commande_service.controller;

import com.example.commande_service.dto.CommandeRequestDTO;
import com.example.commande_service.dto.CommandeResponseDTO;
import com.example.commande_service.dto.LigneCommandeRequestDTO;
import com.example.commande_service.exception.ResourceNotFoundException;
import com.example.commande_service.service.CommandeService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommandeController.class)
class CommandeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommandeService commandeService;

    @Autowired
    private ObjectMapper objectMapper;

    private CommandeResponseDTO buildCommandeResponse(Long id) {
        CommandeResponseDTO dto = new CommandeResponseDTO();
        dto.setId(id);
        dto.setDateCommande(LocalDateTime.of(2024, 1, 15, 10, 0));
        dto.setStatut("EN_ATTENTE");
        dto.setMontantTotal(1999.98);
        dto.setLignes(List.of());
        return dto;
    }

    // ──────────────────────────────────────────────────────────────
    // GET /api/v1/commandes
    // ──────────────────────────────────────────────────────────────

    @Test
    void getAllCommandes_shouldReturn200WithList() throws Exception {
        when(commandeService.getAllCommandes()).thenReturn(List.of(buildCommandeResponse(1L)));

        mockMvc.perform(get("/api/v1/commandes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].statut").value("EN_ATTENTE"))
                .andExpect(jsonPath("$[0].montantTotal").value(1999.98));
    }

    @Test
    void getAllCommandes_shouldReturn200WithEmptyList() throws Exception {
        when(commandeService.getAllCommandes()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/commandes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ──────────────────────────────────────────────────────────────
    // GET /api/v1/commandes/{id}
    // ──────────────────────────────────────────────────────────────

    @Test
    void getCommandeById_shouldReturn200WhenFound() throws Exception {
        when(commandeService.getCommandeById(1L)).thenReturn(buildCommandeResponse(1L));

        mockMvc.perform(get("/api/v1/commandes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE"));
    }

    @Test
    void getCommandeById_shouldReturn404WhenNotFound() throws Exception {
        when(commandeService.getCommandeById(99L))
                .thenThrow(new ResourceNotFoundException("Commande non trouvée avec id: 99"));

        mockMvc.perform(get("/api/v1/commandes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ──────────────────────────────────────────────────────────────
    // POST /api/v1/commandes
    // ──────────────────────────────────────────────────────────────

    @Test
    void createCommande_shouldReturn201WhenValid() throws Exception {
        LigneCommandeRequestDTO ligne = new LigneCommandeRequestDTO(1L, 2);
        CommandeRequestDTO req = new CommandeRequestDTO(List.of(ligne));
        CommandeResponseDTO res = buildCommandeResponse(1L);
        when(commandeService.createCommande(any(CommandeRequestDTO.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/commandes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE"));
    }

    @Test
    void createCommande_shouldReturn400WhenLignesIsEmpty() throws Exception {
        CommandeRequestDTO invalid = new CommandeRequestDTO(List.of());

        mockMvc.perform(post("/api/v1/commandes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createCommande_shouldReturn400WhenLignesIsNull() throws Exception {
        CommandeRequestDTO invalid = new CommandeRequestDTO(null);

        mockMvc.perform(post("/api/v1/commandes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createCommande_shouldReturn400WhenLigneProduitIdIsNull() throws Exception {
        LigneCommandeRequestDTO ligneInvalid = new LigneCommandeRequestDTO(null, 2);
        CommandeRequestDTO invalid = new CommandeRequestDTO(List.of(ligneInvalid));

        mockMvc.perform(post("/api/v1/commandes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createCommande_shouldReturn400WhenLigneQuantiteIsZero() throws Exception {
        LigneCommandeRequestDTO ligneInvalid = new LigneCommandeRequestDTO(1L, 0);
        CommandeRequestDTO invalid = new CommandeRequestDTO(List.of(ligneInvalid));

        mockMvc.perform(post("/api/v1/commandes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ──────────────────────────────────────────────────────────────
    // PATCH /api/v1/commandes/{id}/statut?statut=X
    // ──────────────────────────────────────────────────────────────

    @Test
    void updateStatut_shouldReturn200WhenValid() throws Exception {
        CommandeResponseDTO res = buildCommandeResponse(1L);
        res.setStatut("CONFIRMEE");
        when(commandeService.updateStatut(1L, "CONFIRMEE")).thenReturn(res);

        mockMvc.perform(patch("/api/v1/commandes/1/statut")
                        .param("statut", "CONFIRMEE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("CONFIRMEE"));
    }

    @Test
    void updateStatut_shouldReturn404WhenCommandeNotFound() throws Exception {
        when(commandeService.updateStatut(99L, "CONFIRMEE"))
                .thenThrow(new ResourceNotFoundException("Commande non trouvée avec id: 99"));

        mockMvc.perform(patch("/api/v1/commandes/99/statut")
                        .param("statut", "CONFIRMEE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ──────────────────────────────────────────────────────────────
    // DELETE /api/v1/commandes/{id}
    // ──────────────────────────────────────────────────────────────

    @Test
    void deleteCommande_shouldReturn204WhenFound() throws Exception {
        doNothing().when(commandeService).deleteCommande(1L);

        mockMvc.perform(delete("/api/v1/commandes/1"))
                .andExpect(status().isNoContent());

        verify(commandeService, times(1)).deleteCommande(1L);
    }

    @Test
    void deleteCommande_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Commande non trouvée avec id: 99"))
                .when(commandeService).deleteCommande(99L);

        mockMvc.perform(delete("/api/v1/commandes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
