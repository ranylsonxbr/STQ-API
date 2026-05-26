package com.example.Stq.produto.application.controllers;

import com.example.Stq.config.GlobalExceptionHandler;
import com.example.Stq.produto.application.dto.*;
import com.example.Stq.produto.application.services.ProdutoService;
import com.example.Stq.produto.application.services.VariacaoProdutoService;
import com.example.Stq.produto.domain.UnidadeMedida;
import com.example.Stq.produto.domain.exception.ProdutoComPedidoEmAbertoException;
import com.example.Stq.produto.domain.exception.ProdutoNotFoundException;
import com.example.Stq.produto.domain.exception.VariacaoDuplicadaException;
import com.example.Stq.produto.domain.exception.VariacaoNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProdutoController")
class ProdutoControllerTest {

    @Mock private ProdutoService produtoService;
    @Mock private VariacaoProdutoService variacaoProdutoService;
    @InjectMocks private ProdutoController controller;

    private MockMvc mockMvc;
    private final ObjectMapper json = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    private ProdutoResponse produtoResponse(UUID id) {
        return new ProdutoResponse(id, "PRD-ABC123", "Produto", null,
                UUID.randomUUID(), "Cat", UnidadeMedida.UN, 0, true, Instant.now());
    }

    private ProdutoDetalheResponse produtoDetalhe(UUID id) {
        return new ProdutoDetalheResponse(id, "PRD-ABC123", "Produto", null,
                UUID.randomUUID(), "Cat", UnidadeMedida.UN, 0, true, Instant.now(), List.of());
    }

    @Nested
    @DisplayName("POST /api/produtos")
    class Criar {

        @Test
        @DisplayName("[PROD-C1] deve retornar 201 com Location ao criar produto")
        void deveRetornar201ComLocation() throws Exception {
            UUID id = UUID.randomUUID();
            when(produtoService.criar(any())).thenReturn(produtoResponse(id));

            mockMvc.perform(post("/api/produtos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(
                                    new ProdutoCreateRequest("Produto Teste", null, UUID.randomUUID(), UnidadeMedida.UN, 0))))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString(id.toString())))
                    .andExpect(jsonPath("$.sku").value("PRD-ABC123"));
        }

        @Test
        @DisplayName("deve retornar 400 quando nome tem menos de 3 caracteres")
        void deveRetornar400NomeCurto() throws Exception {
            mockMvc.perform(post("/api/produtos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(
                                    new ProdutoCreateRequest("AB", null, UUID.randomUUID(), UnidadeMedida.UN, 0))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").exists());
        }
    }

    @Nested
    @DisplayName("GET /api/produtos")
    class Listar {

        @Test
        @DisplayName("[PROD-C5] deve retornar 200 com pagina de produtos")
        void deveRetornar200ComPagina() throws Exception {
            UUID id = UUID.randomUUID();
            var page = new PageImpl<>(List.of(produtoResponse(id)), PageRequest.of(0, 20), 1);
            when(produtoService.listar(any(), any(), any(), any())).thenReturn(page);

            mockMvc.perform(get("/api/produtos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].sku").value("PRD-ABC123"));
        }
    }

    @Nested
    @DisplayName("GET /api/produtos/{id}")
    class BuscarPorId {

        @Test
        @DisplayName("[PROD-C6] deve retornar 200 com detalhes do produto")
        void deveRetornar200() throws Exception {
            UUID id = UUID.randomUUID();
            when(produtoService.buscarPorId(id)).thenReturn(produtoDetalhe(id));

            mockMvc.perform(get("/api/produtos/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.variacoes").isArray());
        }

        @Test
        @DisplayName("deve retornar 404 quando produto nao encontrado")
        void deveRetornar404() throws Exception {
            UUID id = UUID.randomUUID();
            when(produtoService.buscarPorId(id)).thenThrow(new ProdutoNotFoundException(id));

            mockMvc.perform(get("/api/produtos/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").exists());
        }
    }

    @Nested
    @DisplayName("GET /api/produtos/sku/{sku}")
    class BuscarPorSku {

        @Test
        @DisplayName("[PROD-C7] deve retornar 200 buscando por SKU")
        void deveRetornar200() throws Exception {
            UUID id = UUID.randomUUID();
            when(produtoService.buscarPorSku("PRD-ABC123")).thenReturn(produtoDetalhe(id));

            mockMvc.perform(get("/api/produtos/sku/PRD-ABC123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sku").value("PRD-ABC123"));
        }
    }

    @Nested
    @DisplayName("PUT /api/produtos/{id}")
    class Atualizar {

        @Test
        @DisplayName("[PROD-C8] deve retornar 200 ao atualizar produto")
        void deveRetornar200() throws Exception {
            UUID id = UUID.randomUUID();
            when(produtoService.atualizar(eq(id), any())).thenReturn(produtoResponse(id));

            mockMvc.perform(put("/api/produtos/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(
                                    new ProdutoUpdateRequest("Novo Nome", null, UUID.randomUUID(), UnidadeMedida.KG, 0, null))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sku").value("PRD-ABC123"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/produtos/{id}")
    class Desativar {

        @Test
        @DisplayName("[PROD-C9] deve retornar 204 ao desativar produto")
        void deveRetornar204() throws Exception {
            UUID id = UUID.randomUUID();
            doNothing().when(produtoService).desativar(id);

            mockMvc.perform(delete("/api/produtos/{id}", id))
                    .andExpect(status().isNoContent());

            verify(produtoService).desativar(id);
        }

        @Test
        @DisplayName("[PROD-C10] deve retornar 409 quando produto tem pedido em aberto")
        void deveRetornar409ComPedidoEmAberto() throws Exception {
            UUID id = UUID.randomUUID();
            doThrow(new ProdutoComPedidoEmAbertoException()).when(produtoService).desativar(id);

            mockMvc.perform(delete("/api/produtos/{id}", id))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail").exists());
        }
    }

    @Nested
    @DisplayName("POST /api/produtos/{id}/variacoes")
    class AdicionarVariacao {

        @Test
        @DisplayName("[VAR-C1] deve retornar 201 ao adicionar variacao")
        void deveRetornar201() throws Exception {
            UUID produtoId = UUID.randomUUID();
            UUID variacaoId = UUID.randomUUID();
            var response = new VariacaoResponse(variacaoId, "Cor", "Azul", "PRD-ABC123-A01", true);
            when(variacaoProdutoService.adicionar(eq(produtoId), any())).thenReturn(response);

            mockMvc.perform(post("/api/produtos/{id}/variacoes", produtoId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(new VariacaoCreateRequest("Cor", "Azul"))))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString(variacaoId.toString())))
                    .andExpect(jsonPath("$.skuVariacao").value("PRD-ABC123-A01"));
        }

        @Test
        @DisplayName("[VAR-C3] deve retornar 409 quando variacao duplicada")
        void deveRetornar409Duplicada() throws Exception {
            UUID produtoId = UUID.randomUUID();
            when(variacaoProdutoService.adicionar(eq(produtoId), any()))
                    .thenThrow(new VariacaoDuplicadaException("Cor", "Azul"));

            mockMvc.perform(post("/api/produtos/{id}/variacoes", produtoId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(new VariacaoCreateRequest("Cor", "Azul"))))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("DELETE /api/produtos/{id}/variacoes/{variacaoId}")
    class DesativarVariacao {

        @Test
        @DisplayName("[VAR-C4] deve retornar 204 ao desativar variacao")
        void deveRetornar204() throws Exception {
            UUID produtoId = UUID.randomUUID();
            UUID variacaoId = UUID.randomUUID();
            doNothing().when(variacaoProdutoService).desativar(produtoId, variacaoId);

            mockMvc.perform(delete("/api/produtos/{id}/variacoes/{variacaoId}", produtoId, variacaoId))
                    .andExpect(status().isNoContent());

            verify(variacaoProdutoService).desativar(produtoId, variacaoId);
        }

        @Test
        @DisplayName("[VAR-C5] deve retornar 404 quando variacao nao pertence ao produto")
        void deveRetornar404() throws Exception {
            UUID produtoId = UUID.randomUUID();
            UUID variacaoId = UUID.randomUUID();
            doThrow(new VariacaoNotFoundException(variacaoId))
                    .when(variacaoProdutoService).desativar(produtoId, variacaoId);

            mockMvc.perform(delete("/api/produtos/{id}/variacoes/{variacaoId}", produtoId, variacaoId))
                    .andExpect(status().isNotFound());
        }
    }
}
