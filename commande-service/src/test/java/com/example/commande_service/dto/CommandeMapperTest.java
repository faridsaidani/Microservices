package com.example.commande_service.dto;

import com.example.commande_service.domain.Commande;
import com.example.commande_service.domain.LigneCommande;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommandeMapperTest {

    private CommandeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CommandeMapper();
    }

    @Test
    @DisplayName("toDTO maps commande with lignes correctly")
    void toDTO_mapsCommandeWithLignes() {
        LigneCommande ligne = new LigneCommande();
        ligne.setId(10L);
        ligne.setProduitId(2L);
        ligne.setProduitNom("Écran");
        ligne.setQuantite(1);
        ligne.setPrixUnitaire(299.99);

        Commande commande = new Commande();
        commande.setId(1L);
        commande.setDateCommande(LocalDateTime.of(2026, 2, 26, 10, 0));
        commande.setStatut(Commande.StatutCommande.EN_ATTENTE);
        commande.setMontantTotal(299.99);
        commande.setLignes(List.of(ligne));

        CommandeResponseDTO dto = mapper.toDTO(commande);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStatut()).isEqualTo("EN_ATTENTE");
        assertThat(dto.getMontantTotal()).isEqualTo(299.99);
        assertThat(dto.getLignes()).hasSize(1);

        LigneCommandeResponseDTO ligneDTO = dto.getLignes().get(0);
        assertThat(ligneDTO.getId()).isEqualTo(10L);
        assertThat(ligneDTO.getProduitId()).isEqualTo(2L);
        assertThat(ligneDTO.getProduitNom()).isEqualTo("Écran");
        assertThat(ligneDTO.getQuantite()).isEqualTo(1);
        assertThat(ligneDTO.getPrixUnitaire()).isEqualTo(299.99);
    }

    @Test
    @DisplayName("toLigneDTO maps all fields correctly")
    void toLigneDTO_mapsAllFields() {
        LigneCommande ligne = new LigneCommande();
        ligne.setId(5L);
        ligne.setProduitId(3L);
        ligne.setProduitNom("Souris");
        ligne.setQuantite(2);
        ligne.setPrixUnitaire(29.99);

        LigneCommandeResponseDTO dto = mapper.toLigneDTO(ligne);

        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getProduitId()).isEqualTo(3L);
        assertThat(dto.getProduitNom()).isEqualTo("Souris");
        assertThat(dto.getQuantite()).isEqualTo(2);
        assertThat(dto.getPrixUnitaire()).isEqualTo(29.99);
    }
}
