package com.storefit.catalog_service.Controller;

import com.storefit.catalog_service.Service.CategoriaService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = CategoriaController.class)
class CategoriaControllerNotFoundTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoriaService categoriaService;

    @Test
    void byId_cuandoNoExiste_retorna404ConMensaje() throws Exception {
        when(categoriaService.findById(888L)).thenThrow(new EntityNotFoundException("Categoría no encontrada: 888"));

        mockMvc.perform(get("/api/v1/categorias/{id}", 888L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Categoría no encontrada: 888"));
    }
}

