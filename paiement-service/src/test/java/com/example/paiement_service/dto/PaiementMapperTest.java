package com.example.paiement_service.dto;

import com.example.paiement_service.domain.Paiement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PaiementMapperTest {

    private PaiementMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PaiementMapper();
    }

    @Test
    @DisplayName("toDTO maps all fields including enum names as strings")
    void toDTO_mapsAllFields() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 26, 12, 0);

        Paiement paiement = new Paiement(
                1L,
                42L,
                1299.99,
                Paiement.ModePaiement.CARTE_BANCAIRE,
                Paiement.StatutPaiement.ACCEPTE,
                now
        );

        PaiementResponseDTO dto = mapper.toDTO(paiement);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getCommandeId()).isEqualTo(42L);
        assertThat(dto.getMontant()).isEqualTo(1299.99);
        assertThat(dto.getModePaiement()).isEqualTo("CARTE_BANCAIRE");
        assertThat(dto.getStatut()).isEqualTo("ACCEPTE");
        assertThat(dto.getDatePaiement()).isEqualTo(now);
    }
}
