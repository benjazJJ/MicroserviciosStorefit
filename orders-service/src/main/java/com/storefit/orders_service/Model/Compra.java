package com.storefit.orders_service.Model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "compra",
        indexes = {
                @Index(name = "idx_compra_rut", columnList = "rutUsuario"),
                @Index(name = "idx_compra_fecha", columnList = "fechaMillis")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Schema(description = "Compra realizada por un usuario, con sus detalles")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID de la compra", example = "100")
    private Long idCompra;          // = @PrimaryKey(autoGenerate = true)

    @NotBlank
    @Column(nullable = false, length = 15)
    @Schema(description = "RUT del usuario comprador", example = "12345678-9")
    private String rutUsuario;      

    @Column(nullable = false)
    @Schema(description = "Fecha/hora de la compra en millis", example = "1717286400000")
    private Long fechaMillis;

    // Relación con los detalles de la compra
    @OneToMany(
            mappedBy = "compra",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonManagedReference("compra-detalles")
    @Builder.Default
    @Schema(description = "Ítems de la compra")
    private List<CompraDetalle> detalles = new ArrayList<>();
}
