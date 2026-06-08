package com.example.Stq.frontend.web;

import com.example.Stq.fornecedor.application.dto.FornecedorResponse;
import com.example.Stq.fornecedor.application.services.FornecedorService;
import com.example.Stq.frontend.support.UsuarioLogado;
import com.example.Stq.pedidocompra.application.dto.PedidoCompraResponse;
import com.example.Stq.pedidocompra.application.services.PedidoCompraService;
import com.example.Stq.pedidocompra.domain.StatusPedido;
import com.example.Stq.produto.application.dto.ProdutoResponse;
import com.example.Stq.produto.application.services.ProdutoService;
import com.example.Stq.produto.domain.UnidadeMedida;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
@DisplayName("PedidoCompraWebController")
class PedidoCompraWebControllerTest {

    @Mock
    private PedidoCompraService pedidoCompraService;
    @Mock
    private FornecedorService fornecedorService;
    @Mock
    private ProdutoService produtoService;
    @Mock
    private UsuarioLogado usuarioLogado;

    @InjectMocks
    private PedidoCompraWebController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private UUID randomId() {
        return UUID.randomUUID();
    }

    private PedidoCompraResponse pedidoResponse(UUID id) {
        return new PedidoCompraResponse(id, randomId(), "Fornecedor Ltda",
                StatusPedido.RASCUNHO, LocalDate.now(), LocalDate.now().plusDays(7),
                "obs", BigDecimal.TEN, List.of(), Instant.now());
    }

    private Page<FornecedorResponse> paginaFornecedores() {
        var f = new FornecedorResponse(randomId(), "Fornecedor Ltda", "11222333000181",
                "f@f.com", "11999", "C", true, Instant.now(), Instant.now(), randomId());
        return new PageImpl<>(List.of(f));
    }

    private Page<ProdutoResponse> paginaProdutos() {
        var p = new ProdutoResponse(randomId(), "PRD-000001", "Prod", null,
                randomId(), "Cat", UnidadeMedida.UN, 5, true, Instant.now());
        return new PageImpl<>(List.of(p));
    }

    private Authentication authMock(UUID usuarioId) {
        Authentication auth = mock(Authentication.class);
        when(usuarioLogado.obterUsuarioId(auth)).thenReturn(usuarioId);
        return auth;
    }

    private void mockListasApoio() {
        when(fornecedorService.listar(any(), any())).thenReturn(paginaFornecedores());
        when(produtoService.listar(any(), any(), any(), any())).thenReturn(paginaProdutos());
    }

    // -------------------------------------------------------------------------
    // GET /web/pedidos
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /web/pedidos")
    class Listar {

        @Test
        @DisplayName("[FW-PC1] deve retornar view 'pedido/lista' com atributo 'pedidos' sem filtro")
        void deveRetornarListaSemFiltro() throws Exception {
            // Arrange
            Page<PedidoCompraResponse> page = new PageImpl<>(List.of(pedidoResponse(randomId())));
            when(pedidoCompraService.listar(any(), any())).thenReturn(page);

            // Act + Assert
            mockMvc.perform(get("/web/pedidos"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("pedido/lista"))
                    .andExpect(model().attributeExists("pedidos"))
                    .andExpect(model().attributeExists("statusPedido"));
        }

        @Test
        @DisplayName("[FW-PC2] deve repassar filtro status ao model quando fornecido")
        void deveRepassarFiltroStatusAoModel() throws Exception {
            // Arrange
            Page<PedidoCompraResponse> page = new PageImpl<>(List.of());
            when(pedidoCompraService.listar(any(), any())).thenReturn(page);

            // Act + Assert
            mockMvc.perform(get("/web/pedidos").param("status", "PENDENTE"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("filtroStatus", StatusPedido.PENDENTE));
        }
    }

    // -------------------------------------------------------------------------
    // GET /web/pedidos/{id}
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /web/pedidos/{id}")
    class Detalhe {

        @Test
        @DisplayName("[FW-PC3] deve retornar view 'pedido/detalhe' com pedido, form de item e produtos")
        void deveRetornarDetalhe() throws Exception {
            // Arrange
            UUID id = randomId();
            when(pedidoCompraService.buscarPorId(id)).thenReturn(pedidoResponse(id));
            when(produtoService.listar(any(), any(), any(), any())).thenReturn(paginaProdutos());

            // Act + Assert
            mockMvc.perform(get("/web/pedidos/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(view().name("pedido/detalhe"))
                    .andExpect(model().attributeExists("pedido"))
                    .andExpect(model().attributeExists("itemPedidoForm"))
                    .andExpect(model().attributeExists("produtos"));
        }
    }

    // -------------------------------------------------------------------------
    // GET /web/pedidos/novo
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /web/pedidos/novo")
    class NovoForm {

        @Test
        @DisplayName("[FW-PC4] deve retornar view 'pedido/form' com pedidoForm e listas de apoio")
        void deveRetornarFormNovo() throws Exception {
            // Arrange
            mockListasApoio();

            // Act + Assert
            mockMvc.perform(get("/web/pedidos/novo"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("pedido/form"))
                    .andExpect(model().attributeExists("pedidoForm"))
                    .andExpect(model().attributeExists("fornecedores"))
                    .andExpect(model().attributeExists("produtos"));
        }
    }

    // -------------------------------------------------------------------------
    // POST /web/pedidos
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /web/pedidos")
    class Criar {

        @Test
        @DisplayName("[FW-PC5] deve redirecionar para detalhe com flash sucesso apos criacao valida")
        void deveRedirecionarComFlashSucessoAposCriacao() throws Exception {
            // Arrange — listas NOT stubbed: controller redirects without fetching lists
            UUID id = randomId();
            UUID usuarioId = randomId();
            Authentication auth = authMock(usuarioId);
            when(pedidoCompraService.criar(any(), eq(usuarioId))).thenReturn(pedidoResponse(id));

            // Act + Assert
            mockMvc.perform(post("/web/pedidos")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("fornecedorId", randomId().toString())
                            .param("dataEmissao", LocalDate.now().toString())
                            .param("itemProdutoId", randomId().toString())
                            .param("itemQuantidade", "2")
                            .param("itemPrecoUnitario", "15.00")
                            .principal(auth))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/pedidos/" + id))
                    .andExpect(flash().attribute("sucesso", "Pedido criado com sucesso."));

            verify(pedidoCompraService).criar(any(), eq(usuarioId));
        }

        @Test
        @DisplayName("[FW-PC6] deve redirecionar para /novo com flash erro quando RuntimeException")
        void deveRedirecionarComFlashErroQuandoRuntimeException() throws Exception {
            // Arrange — listas NOT stubbed: controller redirects to /novo without fetching lists
            UUID usuarioId = randomId();
            Authentication auth = authMock(usuarioId);
            when(pedidoCompraService.criar(any(), eq(usuarioId)))
                    .thenThrow(new RuntimeException("Fornecedor inativo."));

            // Act + Assert
            mockMvc.perform(post("/web/pedidos")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("fornecedorId", randomId().toString())
                            .param("dataEmissao", LocalDate.now().toString())
                            .param("itemProdutoId", randomId().toString())
                            .param("itemQuantidade", "1")
                            .param("itemPrecoUnitario", "10.00")
                            .principal(auth))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/pedidos/novo"))
                    .andExpect(flash().attribute("erro", "Fornecedor inativo."));
        }

        @Test
        @DisplayName("[FW-PC7] deve retornar form com erros quando dados invalidos")
        void deveRetornarFormComErrosQuandoDadosInvalidos() throws Exception {
            // Arrange
            mockListasApoio();

            // Act + Assert — fornecedorId ausente viola @NotNull
            mockMvc.perform(post("/web/pedidos")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("dataEmissao", LocalDate.now().toString()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("pedido/form"))
                    .andExpect(model().attributeExists("fornecedores"))
                    .andExpect(model().attributeExists("produtos"));
        }
    }

    // -------------------------------------------------------------------------
    // POST /web/pedidos/{id}/itens
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /web/pedidos/{id}/itens")
    class AdicionarItem {

        @Test
        @DisplayName("[FW-PC8] deve redirecionar para detalhe com flash sucesso ao adicionar item")
        void deveRedirecionarComFlashSucessoAoAdicionarItem() throws Exception {
            // Arrange
            UUID id = randomId();
            when(pedidoCompraService.adicionarItem(eq(id), any())).thenReturn(pedidoResponse(id));

            // Act + Assert
            mockMvc.perform(post("/web/pedidos/{id}/itens", id)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("produtoId", randomId().toString())
                            .param("quantidade", "5")
                            .param("precoUnitario", "20.00"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/pedidos/" + id))
                    .andExpect(flash().attribute("sucesso", "Item adicionado com sucesso."));
        }

        @Test
        @DisplayName("[FW-PC9] deve redirecionar com flash erro quando RuntimeException ao adicionar item")
        void deveRedirecionarComFlashErroQuandoRuntimeException() throws Exception {
            // Arrange
            UUID id = randomId();
            when(pedidoCompraService.adicionarItem(eq(id), any()))
                    .thenThrow(new RuntimeException("Pedido não está em estado RASCUNHO."));

            // Act + Assert
            mockMvc.perform(post("/web/pedidos/{id}/itens", id)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("produtoId", randomId().toString())
                            .param("quantidade", "2")
                            .param("precoUnitario", "10.00"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/pedidos/" + id))
                    .andExpect(flash().attribute("erro", "Pedido não está em estado RASCUNHO."));
        }

        @Test
        @DisplayName("[FW-PC10] deve retornar detalhe com erros quando dados do item invalidos")
        void deveRetornarDetalheComErrosQuandoDadosInvalidos() throws Exception {
            // Arrange
            UUID id = randomId();
            when(pedidoCompraService.buscarPorId(id)).thenReturn(pedidoResponse(id));
            when(produtoService.listar(any(), any(), any(), any())).thenReturn(paginaProdutos());

            // Act + Assert — quantidade zero viola @Positive
            mockMvc.perform(post("/web/pedidos/{id}/itens", id)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("produtoId", randomId().toString())
                            .param("quantidade", "0")
                            .param("precoUnitario", "10.00"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("pedido/detalhe"))
                    .andExpect(model().attributeExists("pedido"))
                    .andExpect(model().attributeExists("produtos"));
        }
    }

    // -------------------------------------------------------------------------
    // POST /web/pedidos/{id}/itens/{itemId}/remover
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /web/pedidos/{id}/itens/{itemId}/remover")
    class RemoverItem {

        @Test
        @DisplayName("[FW-PC11] deve redirecionar com flash sucesso ao remover item")
        void deveRedirecionarComFlashSucessoAoRemoverItem() throws Exception {
            // Arrange
            UUID id = randomId();
            UUID itemId = randomId();
            when(pedidoCompraService.removerItem(eq(id), eq(itemId))).thenReturn(pedidoResponse(id));

            // Act + Assert
            mockMvc.perform(post("/web/pedidos/{id}/itens/{itemId}/remover", id, itemId))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/pedidos/" + id))
                    .andExpect(flash().attribute("sucesso", "Item removido com sucesso."));

            verify(pedidoCompraService).removerItem(eq(id), eq(itemId));
        }

        @Test
        @DisplayName("[FW-PC12] deve redirecionar com flash erro quando RuntimeException ao remover item")
        void deveRedirecionarComFlashErroQuandoRuntimeException() throws Exception {
            // Arrange
            UUID id = randomId();
            UUID itemId = randomId();
            when(pedidoCompraService.removerItem(eq(id), eq(itemId)))
                    .thenThrow(new RuntimeException("Item nao pertence ao pedido."));

            // Act + Assert
            mockMvc.perform(post("/web/pedidos/{id}/itens/{itemId}/remover", id, itemId))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/pedidos/" + id))
                    .andExpect(flash().attribute("erro", "Item nao pertence ao pedido."));
        }
    }

    // -------------------------------------------------------------------------
    // POST /web/pedidos/{id}/enviar
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /web/pedidos/{id}/enviar")
    class Enviar {

        @Test
        @DisplayName("[FW-PC13] deve redirecionar com flash sucesso ao enviar pedido")
        void deveRedirecionarComFlashSucessoAoEnviar() throws Exception {
            // Arrange
            UUID id = randomId();
            when(pedidoCompraService.enviar(id)).thenReturn(pedidoResponse(id));

            // Act + Assert
            mockMvc.perform(post("/web/pedidos/{id}/enviar", id))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/pedidos/" + id))
                    .andExpect(flash().attribute("sucesso", "Pedido enviado para aprovação."));

            verify(pedidoCompraService).enviar(id);
        }

        @Test
        @DisplayName("[FW-PC14] deve redirecionar com flash erro quando RuntimeException ao enviar")
        void deveRedirecionarComFlashErroQuandoRuntimeException() throws Exception {
            // Arrange
            UUID id = randomId();
            when(pedidoCompraService.enviar(id))
                    .thenThrow(new RuntimeException("Pedido sem itens."));

            // Act + Assert
            mockMvc.perform(post("/web/pedidos/{id}/enviar", id))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/pedidos/" + id))
                    .andExpect(flash().attribute("erro", "Pedido sem itens."));
        }
    }

    // -------------------------------------------------------------------------
    // POST /web/pedidos/{id}/aprovar
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /web/pedidos/{id}/aprovar")
    class Aprovar {

        @Test
        @DisplayName("[FW-PC15] deve redirecionar com flash sucesso ao aprovar pedido (RN-10)")
        void deveRedirecionarComFlashSucessoAoAprovar() throws Exception {
            // Arrange
            UUID id = randomId();
            when(pedidoCompraService.aprovar(id)).thenReturn(pedidoResponse(id));

            // Act + Assert
            mockMvc.perform(post("/web/pedidos/{id}/aprovar", id))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/pedidos/" + id))
                    .andExpect(flash().attribute("sucesso", "Pedido aprovado com sucesso."));

            verify(pedidoCompraService).aprovar(id);
        }

        @Test
        @DisplayName("[FW-PC16] deve redirecionar com flash erro quando RuntimeException ao aprovar")
        void deveRedirecionarComFlashErroQuandoRuntimeException() throws Exception {
            // Arrange
            UUID id = randomId();
            when(pedidoCompraService.aprovar(id))
                    .thenThrow(new RuntimeException("Pedido nao esta PENDENTE."));

            // Act + Assert
            mockMvc.perform(post("/web/pedidos/{id}/aprovar", id))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/pedidos/" + id))
                    .andExpect(flash().attribute("erro", "Pedido nao esta PENDENTE."));
        }
    }

    // -------------------------------------------------------------------------
    // POST /web/pedidos/{id}/receber
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /web/pedidos/{id}/receber")
    class Receber {

        @Test
        @DisplayName("[FW-PC17] deve redirecionar com flash sucesso ao receber pedido (RN-05 + RN-06)")
        void deveRedirecionarComFlashSucessoAoReceber() throws Exception {
            // Arrange
            UUID id = randomId();
            UUID usuarioId = randomId();
            Authentication auth = authMock(usuarioId);
            when(pedidoCompraService.receber(eq(id), eq(usuarioId))).thenReturn(pedidoResponse(id));

            // Act + Assert
            mockMvc.perform(post("/web/pedidos/{id}/receber", id)
                            .principal(auth))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/pedidos/" + id))
                    .andExpect(flash().attribute("sucesso",
                            "Pedido recebido. Entradas registradas no estoque."));

            verify(pedidoCompraService).receber(eq(id), eq(usuarioId));
        }

        @Test
        @DisplayName("[FW-PC18] deve redirecionar com flash erro quando RuntimeException ao receber (RN-05)")
        void deveRedirecionarComFlashErroQuandoRuntimeException() throws Exception {
            // Arrange
            UUID id = randomId();
            UUID usuarioId = randomId();
            Authentication auth = authMock(usuarioId);
            when(pedidoCompraService.receber(eq(id), eq(usuarioId)))
                    .thenThrow(new RuntimeException("Pedido nao esta APROVADO."));

            // Act + Assert
            mockMvc.perform(post("/web/pedidos/{id}/receber", id)
                            .principal(auth))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/pedidos/" + id))
                    .andExpect(flash().attribute("erro", "Pedido nao esta APROVADO."));
        }
    }

    // -------------------------------------------------------------------------
    // POST /web/pedidos/{id}/cancelar
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /web/pedidos/{id}/cancelar")
    class Cancelar {

        @Test
        @DisplayName("[FW-PC19] deve redirecionar com flash sucesso ao cancelar pedido")
        void deveRedirecionarComFlashSucessoAoCancelar() throws Exception {
            // Arrange
            UUID id = randomId();
            when(pedidoCompraService.cancelar(id)).thenReturn(pedidoResponse(id));

            // Act + Assert
            mockMvc.perform(post("/web/pedidos/{id}/cancelar", id))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/pedidos/" + id))
                    .andExpect(flash().attribute("sucesso", "Pedido cancelado."));

            verify(pedidoCompraService).cancelar(id);
        }

        @Test
        @DisplayName("[FW-PC20] deve redirecionar com flash erro quando RuntimeException ao cancelar")
        void deveRedirecionarComFlashErroQuandoRuntimeException() throws Exception {
            // Arrange
            UUID id = randomId();
            when(pedidoCompraService.cancelar(id))
                    .thenThrow(new RuntimeException("Pedido APROVADO nao pode ser cancelado."));

            // Act + Assert
            mockMvc.perform(post("/web/pedidos/{id}/cancelar", id))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/pedidos/" + id))
                    .andExpect(flash().attribute("erro", "Pedido APROVADO nao pode ser cancelado."));
        }
    }
}
