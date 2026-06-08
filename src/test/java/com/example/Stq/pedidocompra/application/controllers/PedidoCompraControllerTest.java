package com.example.Stq.pedidocompra.application.controllers;

import com.example.Stq.autenticacao.domain.Perfil;
import com.example.Stq.autenticacao.domain.Usuario;
import com.example.Stq.autenticacao.infra.UsuarioDetails;
import com.example.Stq.config.GlobalExceptionHandler;
import com.example.Stq.pedidocompra.application.dto.CriarPedidoRequest;
import com.example.Stq.pedidocompra.application.dto.ItemRequest;
import com.example.Stq.pedidocompra.application.dto.PedidoCompraResponse;
import com.example.Stq.pedidocompra.application.services.PedidoCompraService;
import com.example.Stq.pedidocompra.domain.PedidoCompraFiltro;
import com.example.Stq.pedidocompra.domain.StatusPedido;
import com.example.Stq.pedidocompra.domain.exception.PedidoCompraNotFoundException;
import com.example.Stq.pedidocompra.domain.exception.TransicaoStatusInvalidaException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("PedidoCompraController")
class PedidoCompraControllerTest {

    @Mock private PedidoCompraService pedidoCompraService;
    @InjectMocks private PedidoCompraController controller;

    private MockMvc mockMvc;
    private final ObjectMapper json = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final UUID PEDIDO_ID = UUID.fromString("00000000-0000-0000-0000-0000000000d1");
    private static final UUID FORNECEDOR_ID = UUID.fromString("00000000-0000-0000-0000-0000000000f1");
    private static final UUID PRODUTO_ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final UUID USUARIO_ID = UUID.fromString("00000000-0000-0000-0000-0000000000b2");

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new AuthenticationPrincipalArgumentResolver())
                .build();
        autenticar();
    }

    private void autenticar() {
        Usuario usuario = Usuario.builder()
                .id(USUARIO_ID).nome("Teste").email("t@t.com")
                .senha("hash").perfil(Perfil.ADMIN).ativo(true).build();
        UsuarioDetails details = new UsuarioDetails(usuario);
        var auth = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private PedidoCompraResponse response(StatusPedido status) {
        return new PedidoCompraResponse(
                PEDIDO_ID, FORNECEDOR_ID, "ACME", status,
                LocalDate.now(), null, "obs", new BigDecimal("20.00"),
                List.of(), Instant.now());
    }

    @Nested
    @DisplayName("POST /api/pedidos-compra")
    class Criar {

        @Test
        @DisplayName("[PC-C1] deve criar pedido e retornar 201 com Location")
        void deveCriar201ComLocation() throws Exception {
            when(pedidoCompraService.criar(any(CriarPedidoRequest.class), eq(USUARIO_ID)))
                    .thenReturn(response(StatusPedido.RASCUNHO));
            var req = new CriarPedidoRequest(FORNECEDOR_ID, LocalDate.now(), null, "obs",
                    List.of(new ItemRequest(PRODUTO_ID, null, 2, new BigDecimal("10.00"))));

            mockMvc.perform(post("/api/pedidos-compra")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location",
                            org.hamcrest.Matchers.containsString("/api/pedidos-compra/" + PEDIDO_ID)))
                    .andExpect(jsonPath("$.status").value("RASCUNHO"));
        }

        @Test
        @DisplayName("[PC-A5] body sem itens deve retornar 400")
        void bodySemItensDeveRetornar400() throws Exception {
            var req = new CriarPedidoRequest(FORNECEDOR_ID, LocalDate.now(), null, null, List.of());

            mockMvc.perform(post("/api/pedidos-compra")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }
    }

    @Nested
    @DisplayName("GET /api/pedidos-compra")
    class Consultar {

        @Test
        @DisplayName("[PC-C9] listar deve retornar 200 paginado")
        void listarDeveRetornar200() throws Exception {
            var page = new PageImpl<>(List.of(response(StatusPedido.PENDENTE)), PageRequest.of(0, 20), 1);
            when(pedidoCompraService.listar(any(PedidoCompraFiltro.class), any())).thenReturn(page);

            mockMvc.perform(get("/api/pedidos-compra").param("status", "PENDENTE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].status").value("PENDENTE"));
        }

        @Test
        @DisplayName("[PC-A4] buscar id inexistente deve retornar 404")
        void buscarInexistenteDeveRetornar404() throws Exception {
            when(pedidoCompraService.buscarPorId(PEDIDO_ID))
                    .thenThrow(new PedidoCompraNotFoundException(PEDIDO_ID));

            mockMvc.perform(get("/api/pedidos-compra/" + PEDIDO_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("[PC-C1b] buscar por id existente deve retornar 200")
        void buscarPorIdDeveRetornar200() throws Exception {
            when(pedidoCompraService.buscarPorId(PEDIDO_ID)).thenReturn(response(StatusPedido.RASCUNHO));

            mockMvc.perform(get("/api/pedidos-compra/" + PEDIDO_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(PEDIDO_ID.toString()));
        }
    }

    @Nested
    @DisplayName("itens")
    class Itens {

        @Test
        @DisplayName("[PC-C2] adicionar item deve retornar 200")
        void adicionarItemDeveRetornar200() throws Exception {
            when(pedidoCompraService.adicionarItem(eq(PEDIDO_ID), any(ItemRequest.class)))
                    .thenReturn(response(StatusPedido.RASCUNHO));
            var req = new ItemRequest(PRODUTO_ID, null, 2, new BigDecimal("10.00"));

            mockMvc.perform(post("/api/pedidos-compra/" + PEDIDO_ID + "/itens")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(req)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("[PC-C2b] remover item deve retornar 200")
        void removerItemDeveRetornar200() throws Exception {
            UUID itemId = UUID.fromString("00000000-0000-0000-0000-0000000000e1");
            when(pedidoCompraService.removerItem(PEDIDO_ID, itemId))
                    .thenReturn(response(StatusPedido.RASCUNHO));

            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .delete("/api/pedidos-compra/" + PEDIDO_ID + "/itens/" + itemId))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("[PC-C4c] enviar deve retornar 200 com status PENDENTE")
        void enviarDeveRetornar200() throws Exception {
            when(pedidoCompraService.enviar(PEDIDO_ID)).thenReturn(response(StatusPedido.PENDENTE));

            mockMvc.perform(patch("/api/pedidos-compra/" + PEDIDO_ID + "/enviar"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PENDENTE"));
        }
    }

    @Nested
    @DisplayName("transições")
    class Transicoes {

        @Test
        @DisplayName("[PC-C5] aprovar deve retornar 200 com status APROVADO")
        void aprovarDeveRetornar200() throws Exception {
            when(pedidoCompraService.aprovar(PEDIDO_ID)).thenReturn(response(StatusPedido.APROVADO));

            mockMvc.perform(patch("/api/pedidos-compra/" + PEDIDO_ID + "/aprovar"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("APROVADO"));
        }

        @Test
        @DisplayName("[PC-C8] cancelar pedido aprovado deve retornar 409")
        void cancelarAprovadoDeveRetornar409() throws Exception {
            when(pedidoCompraService.cancelar(PEDIDO_ID))
                    .thenThrow(new TransicaoStatusInvalidaException(StatusPedido.APROVADO, "cancelar"));

            mockMvc.perform(patch("/api/pedidos-compra/" + PEDIDO_ID + "/cancelar"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        @DisplayName("[PC-C6] receber deve retornar 200 com status RECEBIDO")
        void receberDeveRetornar200() throws Exception {
            when(pedidoCompraService.receber(eq(PEDIDO_ID), eq(USUARIO_ID)))
                    .thenReturn(response(StatusPedido.RECEBIDO));

            mockMvc.perform(patch("/api/pedidos-compra/" + PEDIDO_ID + "/receber"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("RECEBIDO"));
        }
    }
}
