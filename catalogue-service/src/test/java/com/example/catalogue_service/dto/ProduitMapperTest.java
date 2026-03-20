package com.example.catalogue_service.dto;

import com.example.catalogue_service.domain.Produit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProduitMapperTest {

    private ProduitMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProduitMapper();
    }

    @Test
    @DisplayName("toDTO maps all fields correctly")
    void toDTO_mapsAllFields() {
        Produit produit = new Produit(1L, "Laptop", "Top spec", 999.99, 10);

        ProduitResponseDTO dto = mapper.toDTO(produit);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getNom()).isEqualTo("Laptop");
        assertThat(dto.getDescription()).isEqualTo("Top spec");
        assertThat(dto.getPrix()).isEqualTo(999.99);
        assertThat(dto.getQuantiteStock()).isEqualTo(10);
    }

    @Test
    @DisplayName("toEntity maps request DTO to entity without id")
    void toEntity_mapsWithoutId() {
        ProduitRequestDTO dto = new ProduitRequestDTO("Clavier", "Mécanique", 79.90, 50);

        Produit produit = mapper.toEntity(dto);

        assertThat(produit.getId()).isNull();
        assertThat(produit.getNom()).isEqualTo("Clavier");
        assertThat(produit.getDescription()).isEqualTo("Mécanique");
        assertThat(produit.getPrix()).isEqualTo(79.90);
        assertThat(produit.getQuantiteStock()).isEqualTo(50);
    }

    @Test
    @DisplayName("updateEntity overwrites all mutable fields")
    void updateEntity_overwritesFields() {
        Produit produit = new Produit(1L, "Old", "Old desc", 1.0, 1);
        ProduitRequestDTO dto = new ProduitRequestDTO("New", "New desc", 2.0, 5);

        mapper.updateEntity(produit, dto);

        assertThat(produit.getNom()).isEqualTo("New");
        assertThat(produit.getDescription()).isEqualTo("New desc");
        assertThat(produit.getPrix()).isEqualTo(2.0);
        assertThat(produit.getQuantiteStock()).isEqualTo(5);
        assertThat(produit.getId()).isEqualTo(1L); // id must not change
    }
}
