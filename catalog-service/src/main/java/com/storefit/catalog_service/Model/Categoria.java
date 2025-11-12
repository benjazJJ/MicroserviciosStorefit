package com.storefit.catalog_service.Model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "categoria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representa una categoría de productos de la tienda (ej: Poleras, Zapatillas)")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    @Schema(description = "ID único de la categoría", example = "1")
    private Long idCategoria;

    @NotBlank
    @Column(name = "nombre_categoria", nullable = false, unique = true, length = 50)
    @Schema(description = "Nombre de la categoría", example = "Poleras")
    private String nombreCategoria;
}