package com.storefit.orders_service.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "compra_detalle",
        indexes = {
                @Index(name = "idx_det_compra", columnList = "idCompra"),
                @Index(name = "idx_det_producto", columnList = "idProducto")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Schema(description = "Detalle de un ítem dentro de una compra")
public class CompraDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID del detalle", example = "1")
    private Long idDetalle;          // = @PrimaryKey(autoGenerate = true)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCompra", nullable = false)
    @JsonBackReference("compra-detalles")
    @Schema(description = "Compra a la que pertenece este detalle")
    private Compra compra;           

    @Column(nullable = false)
    @Schema(description = "ID del producto (id_producto)", example = "1001")
    private Long idProducto;

    @NotBlank
    @Column(nullable = false, length = 150)
    @Schema(description = "Nombre del producto", example = "Zapatillas Runner X")
    private String nombreProducto;

    @Min(1)
    @Column(nullable = false)
    @Schema(description = "Cantidad comprada", example = "2")
    private Integer cantidad;

    @Min(0)
    @Column(nullable = false)
    @Schema(description = "Precio unitario CLP", example = "19990")
    private Integer precioUnitario;  //clp
}
