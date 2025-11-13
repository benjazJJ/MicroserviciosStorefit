package com.storefit.catalog_service.Model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonAlias;
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
    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    @JsonAlias("nombre")
    @Schema(description = "Nombre de la categoría", example = "Poleras")
    private String nombreCategoria;
}
