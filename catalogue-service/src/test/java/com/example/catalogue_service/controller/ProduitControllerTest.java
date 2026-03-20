package com.example.catalogue_service.controller;

import com.example.catalogue_service.dto.ProduitRequestDTO;
import com.example.catalogue_service.dto.ProduitResponseDTO;
import com.example.catalogue_service.exception.ResourceNotFoundException;
import com.example.catalogue_service.service.ProduitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProduitController.class)
class ProduitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProduitService produitService;

    @Autowired
    private ObjectMapper objectMapper;

    // ──────────────────────────────────────────────────────────────
    // GET /api/v1/produits
    // ──────────────────────────────────────────────────────────────

    @Test
    void getAllProduits_shouldReturn200WithList() throws Exception {
        ProduitResponseDTO p = new ProduitResponseDTO(1L, "Laptop", "High-end laptop", 999.99, 10);
        when(produitService.getAllProduits()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/v1/produits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nom").value("Laptop"))
                .andExpect(jsonPath("$[0].prix").value(999.99));
    }

    @Test
    void getAllProduits_shouldReturn200WithEmptyList() throws Exception {
        when(produitService.getAllProduits()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/produits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ──────────────────────────────────────────────────────────────
    // GET /api/v1/produits/{id}
    // ──────────────────────────────────────────────────────────────

    @Test
    void getProduitById_shouldReturn200WhenFound() throws Exception {
        ProduitResponseDTO p = new ProduitResponseDTO(1L, "Laptop", "High-end laptop", 999.99, 10);
        when(produitService.getProduitById(1L)).thenReturn(p);

        mockMvc.perform(get("/api/v1/produits/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nom").value("Laptop"))
                .andExpect(jsonPath("$.quantiteStock").value(10));
    }

    @Test
    void getProduitById_shouldReturn404WhenNotFound() throws Exception {
        when(produitService.getProduitById(99L))
                .thenThrow(new ResourceNotFoundException("Produit non trouvé avec id: 99"));

        mockMvc.perform(get("/api/v1/produits/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // ──────────────────────────────────────────────────────────────
    // POST /api/v1/produits
    // ──────────────────────────────────────────────────────────────

    @Test
    void createProduit_shouldReturn201WhenValid() throws Exception {
        ProduitRequestDTO req = new ProduitRequestDTO("Laptop", "High-end laptop", 999.99, 10);
        ProduitResponseDTO res = new ProduitResponseDTO(1L, "Laptop", "High-end laptop", 999.99, 10);
        when(produitService.createProduit(any(ProduitRequestDTO.class))).thenReturn(res);

        mockMvc.perform(post("/api/v1/produits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nom").value("Laptop"));
    }

    @Test
    void createProduit_shouldReturn400WhenNomIsBlank() throws Exception {
        ProduitRequestDTO invalid = new ProduitRequestDTO("", "desc", 100.0, 5);

        mockMvc.perform(post("/api/v1/produits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.nom").exists());
    }

    @Test
    void createProduit_shouldReturn400WhenPrixIsNull() throws Exception {
        ProduitRequestDTO invalid = new ProduitRequestDTO("Laptop", "desc", null, 5);

        mockMvc.perform(post("/api/v1/produits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createProduit_shouldReturn400WhenPrixIsNegative() throws Exception {
        ProduitRequestDTO invalid = new ProduitRequestDTO("Laptop", "desc", -1.0, 5);

        mockMvc.perform(post("/api/v1/produits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.prix").exists());
    }

    @Test
    void createProduit_shouldReturn400WhenQuantiteIsNegative() throws Exception {
        ProduitRequestDTO invalid = new ProduitRequestDTO("Laptop", "desc", 100.0, -1);

        mockMvc.perform(post("/api/v1/produits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.quantiteStock").exists());
    }

    // ──────────────────────────────────────────────────────────────
    // PUT /api/v1/produits/{id}
    // ──────────────────────────────────────────────────────────────

    @Test
    void updateProduit_shouldReturn200WhenValid() throws Exception {
        ProduitRequestDTO req = new ProduitRequestDTO("Laptop Pro", "Updated", 1199.99, 5);
        ProduitResponseDTO res = new ProduitResponseDTO(1L, "Laptop Pro", "Updated", 1199.99, 5);
        when(produitService.updateProduit(eq(1L), any(ProduitRequestDTO.class))).thenReturn(res);

        mockMvc.perform(put("/api/v1/produits/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Laptop Pro"))
                .andExpect(jsonPath("$.prix").value(1199.99));
    }

    @Test
    void updateProduit_shouldReturn404WhenNotFound() throws Exception {
        ProduitRequestDTO req = new ProduitRequestDTO("Laptop Pro", "Updated", 1199.99, 5);
        when(produitService.updateProduit(eq(99L), any(ProduitRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Produit non trouvé avec id: 99"));

        mockMvc.perform(put("/api/v1/produits/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ──────────────────────────────────────────────────────────────
    // DELETE /api/v1/produits/{id}
    // ──────────────────────────────────────────────────────────────

    @Test
    void deleteProduit_shouldReturn204WhenFound() throws Exception {
        doNothing().when(produitService).deleteProduit(1L);

        mockMvc.perform(delete("/api/v1/produits/1"))
                .andExpect(status().isNoContent());

        verify(produitService, times(1)).deleteProduit(1L);
    }

    @Test
    void deleteProduit_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Produit non trouvé avec id: 99"))
                .when(produitService).deleteProduit(99L);

        mockMvc.perform(delete("/api/v1/produits/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
