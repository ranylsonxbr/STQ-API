package com.example.Stq.produto.application.controllers;

import com.example.Stq.config.GlobalExceptionHandler;
import com.example.Stq.produto.application.dto.CategoriaCreateRequest;
import com.example.Stq.produto.application.dto.CategoriaResponse;
import com.example.Stq.produto.application.dto.CategoriaUpdateRequest;
import com.example.Stq.produto.application.services.CategoriaService;
import com.example.Stq.produto.domain.exception.CategoriaComFilhasException;
import com.example.Stq.produto.domain.exception.CategoriaInativaException;
import com.example.Stq.produto.domain.exception.CategoriaNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoriaController")
class CategoriaControllerTest {

    @Mock private CategoriaService categoriaService;
    @InjectMocks private CategoriaController controller;

    private MockMvc mockMvc;
    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    private CategoriaResponse categoriaResponse(UUID id, String nome) {
        return new CategoriaResponse(id, nome, null, true);
    }

    @Nested
    @DisplayName("POST /api/categorias")
    class Criar {

        @Test
        @DisplayName("[US-A1] deve retornar 201 com Location ao criar categoria")
        void deveRetornar201ComLocation() throws Exception {
            UUID id = UUID.randomUUID();
            when(categoriaService.criar(any())).thenReturn(categoriaResponse(id, "Eletrônicos"));

            mockMvc.perform(post("/api/categorias")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(new CategoriaCreateRequest("Eletrônicos", null))))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", org.hamcrest.Matchers.containsString(id.toString())))
                    .andExpect(jsonPath("$.nome").value("Eletrônicos"));
        }

        @Test
        @DisplayName("[EB-2] deve retornar 409 quando categoria pai está inativa")
        void deveRetornar409PaiInativo() throws Exception {
            when(categoriaService.criar(any())).thenThrow(new CategoriaInativaException());

            mockMvc.perform(post("/api/categorias")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(new CategoriaCreateRequest("Filho", UUID.randomUUID()))))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("deve retornar 400 quando nome está em branco")
        void deveRetornar400NomeEmBranco() throws Exception {
            mockMvc.perform(post("/api/categorias")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nome\":\"\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").exists());
        }
    }

    @Nested
    @DisplayName("GET /api/categorias")
    class Listar {

        @Test
        @DisplayName("[US-A3] deve retornar 200 com pagina de categorias")
        void deveRetornar200ComPagina() throws Exception {
            var page = new PageImpl<>(
                    List.of(categoriaResponse(UUID.randomUUID(), "Cat A")),
                    PageRequest.of(0, 20), 1
            );
            when(categoriaService.listar(any(), any(), any())).thenReturn(page);

            mockMvc.perform(get("/api/categorias"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].nome").value("Cat A"));
        }
    }

    @Nested
    @DisplayName("GET /api/categorias/{id}")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar 200 com categoria quando encontrada")
        void deveRetornar200() throws Exception {
            UUID id = UUID.randomUUID();
            when(categoriaService.buscarPorId(id)).thenReturn(categoriaResponse(id, "Eletrônicos"));

            mockMvc.perform(get("/api/categorias/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }

        @Test
        @DisplayName("deve retornar 404 quando categoria nao encontrada")
        void deveRetornar404() throws Exception {
            UUID id = UUID.randomUUID();
            when(categoriaService.buscarPorId(id)).thenThrow(new CategoriaNotFoundException(id));

            mockMvc.perform(get("/api/categorias/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").exists());
        }
    }

    @Nested
    @DisplayName("PUT /api/categorias/{id}")
    class Atualizar {

        @Test
        @DisplayName("[PROD-C8] deve retornar 200 ao atualizar categoria")
        void deveRetornar200() throws Exception {
            UUID id = UUID.randomUUID();
            when(categoriaService.atualizar(eq(id), any()))
                    .thenReturn(categoriaResponse(id, "Novo Nome"));

            mockMvc.perform(put("/api/categorias/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(new CategoriaUpdateRequest("Novo Nome", null))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("Novo Nome"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/categorias/{id}")
    class Desativar {

        @Test
        @DisplayName("[US-A5] deve retornar 204 ao desativar categoria")
        void deveRetornar204() throws Exception {
            UUID id = UUID.randomUUID();
            doNothing().when(categoriaService).desativar(id);

            mockMvc.perform(delete("/api/categorias/{id}", id))
                    .andExpect(status().isNoContent());

            verify(categoriaService).desativar(id);
        }

        @Test
        @DisplayName("[EB-10] deve retornar 409 quando categoria tem filhas ativas")
        void deveRetornar409ComFilhas() throws Exception {
            UUID id = UUID.randomUUID();
            doThrow(new CategoriaComFilhasException()).when(categoriaService).desativar(id);

            mockMvc.perform(delete("/api/categorias/{id}", id))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail").exists());
        }
    }
}
