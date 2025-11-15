package com.storefit.orders_service.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;

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
public class CompraDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetalle;          // = @PrimaryKey(autoGenerate = true)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCompra", nullable = false)
    @JsonBackReference("compra-detalles")
    private Compra compra;           

    @Column(nullable = false)
    private Long idProducto;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String nombreProducto;

    @Min(1)
    @Column(nullable = false)
    private Integer cantidad;

    @Min(0)
    @Column(nullable = false)
    private Integer precioUnitario;  //clp
}
