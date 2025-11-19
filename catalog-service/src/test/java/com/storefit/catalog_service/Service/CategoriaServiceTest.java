package com.storefit.catalog_service.Service;

import com.storefit.catalog_service.Model.Categoria;
import com.storefit.catalog_service.Repository.CategoriaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository repo;

    @InjectMocks
    private CategoriaService service;

    @Test
    void findAll_devuelveLista() {
        when(repo.findAll()).thenReturn(List.of(new Categoria(1L, "Zapatillas")));
        assertThat(service.findAll()).hasSize(1);
    }

    @Test
    void findById_encontrada() {
        var c = new Categoria(1L, "Zapatillas");
        when(repo.findById(1L)).thenReturn(Optional.of(c));
        var out = service.findById(1L);
        assertThat(out.getIdCategoria()).isEqualTo(1L);
        assertThat(out.getNombreCategoria()).isEqualTo("Zapatillas");
    }

    @Test
    void findById_noExiste_lanzaEntityNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("no encontrada");
    }

    @Test
    void create_ok_normalizaYGuarda() {
        var in = new Categoria(null, "  Zapatillas  ");
        when(repo.existsByNombreCategoriaIgnoreCase("Zapatillas")).thenReturn(false);
        when(repo.save(any(Categoria.class))).thenAnswer(inv -> {
            Categoria c = inv.getArgument(0);
            c.setIdCategoria(1L);
            return c;
        });
        var out = service.create(in);
        assertThat(out.getIdCategoria()).isEqualTo(1L);
        assertThat(out.getNombreCategoria()).isEqualTo("Zapatillas");
    }

    @Test
    void create_duplicado_lanzaIllegalArgument() {
        var in = new Categoria(null, "Zapatillas");
        when(repo.existsByNombreCategoriaIgnoreCase("Zapatillas")).thenReturn(true);
        assertThatThrownBy(() -> service.create(in))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya existe");
    }

    @Test
    void update_ok_cambiaNombre() {
        var db = new Categoria(1L, "OldName");
        when(repo.findById(1L)).thenReturn(Optional.of(db));
        when(repo.existsByNombreCategoriaIgnoreCase("Poleras")).thenReturn(false);
        when(repo.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        var out = service.update(1L, new Categoria(null, "Poleras"));
        assertThat(out.getNombreCategoria()).isEqualTo("Poleras");
    }

    @Test
    void update_conflictoNombre_lanzaIllegalArgument() {
        var db = new Categoria(1L, "OldName");
        when(repo.findById(1L)).thenReturn(Optional.of(db));
        when(repo.existsByNombreCategoriaIgnoreCase("Zapatillas")).thenReturn(true);
        assertThatThrownBy(() -> service.update(1L, new Categoria(null, "Zapatillas")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya utilizado");
    }

    @Test
    void delete_ok_elimina() {
        var db = new Categoria(1L, "Zapatillas");
        when(repo.findById(1L)).thenReturn(Optional.of(db));
        doNothing().when(repo).delete(db);
        service.delete(1L);
        verify(repo, times(1)).delete(db);
    }
}

