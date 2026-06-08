package com.example.Stq.pedidocompra;

import com.example.Stq.autenticacao.domain.Perfil;
import com.example.Stq.autenticacao.domain.Usuario;
import com.example.Stq.autenticacao.infra.UsuarioDetails;
import com.example.Stq.autenticacao.infra.UsuarioJpaRepository;
import com.example.Stq.fornecedor.domain.Fornecedor;
import com.example.Stq.fornecedor.infra.FornecedorJpaRepository;
import com.example.Stq.movimentacao.domain.OrigemMovimentacao;
import com.example.Stq.movimentacao.infra.EstoqueJpaRepository;
import com.example.Stq.movimentacao.infra.MovimentacaoJpaRepository;
import com.example.Stq.pedidocompra.application.dto.CriarPedidoRequest;
import com.example.Stq.pedidocompra.application.dto.ItemRequest;
import com.example.Stq.pedidocompra.infra.PedidoCompraJpaRepository;
import com.example.Stq.produto.domain.Categoria;
import com.example.Stq.produto.domain.Produto;
import com.example.Stq.produto.domain.UnidadeMedida;
import com.example.Stq.produto.infra.CategoriaJpaRepository;
import com.example.Stq.produto.infra.ProdutoJpaRepository;
import com.example.Stq.produto.infra.VariacaoProdutoJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("PedidoCompraIntegrationTest")
class PedidoCompraIntegrationTest {

    @Autowired WebApplicationContext wac;
    @Autowired CategoriaJpaRepository categoriaJpaRepository;
    @Autowired ProdutoJpaRepository produtoJpaRepository;
    @Autowired VariacaoProdutoJpaRepository variacaoProdutoJpaRepository;
    @Autowired FornecedorJpaRepository fornecedorJpaRepository;
    @Autowired PedidoCompraJpaRepository pedidoCompraJpaRepository;
    @Autowired EstoqueJpaRepository estoqueJpaRepository;
    @Autowired MovimentacaoJpaRepository movimentacaoJpaRepository;
    @Autowired UsuarioJpaRepository usuarioJpaRepository;

    private MockMvc mockMvc;
    private final ObjectMapper json = new ObjectMapper().registerModule(new JavaTimeModule());

    private UUID produtoId;
    private UUID fornecedorId;
    private RequestPostProcessor operador;
    private RequestPostProcessor admin;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
        limpar();

        Categoria categoria = categoriaJpaRepository.save(
                Categoria.builder().nome("Cat Pedido").ativo(true).build());
        Produto produto = produtoJpaRepository.save(Produto.builder()
                .sku("PRD-PED001").nome("Produto Pedido").categoria(categoria)
                .unidadeMedida(UnidadeMedida.UN).estoqueMinimo(5).ativo(true).build());
        produtoId = produto.getId();

        Fornecedor fornecedor = fornecedorJpaRepository.save(Fornecedor.builder()
                .razaoSocial("Fornecedor Pedido").cnpj("11222333000181").ativo(true).build());
        fornecedorId = fornecedor.getId();

        operador = autenticar(criarUsuario("operador@teste.com", Perfil.OPERADOR));
        admin = autenticar(criarUsuario("admin@teste.com", Perfil.ADMIN));
    }

    @AfterEach
    void tearDown() {
        limpar();
    }

    private void limpar() {
        movimentacaoJpaRepository.deleteAll();
        estoqueJpaRepository.deleteAll();
        pedidoCompraJpaRepository.deleteAll();
        variacaoProdutoJpaRepository.deleteAll();
        produtoJpaRepository.deleteAll();
        categoriaJpaRepository.deleteAll();
        fornecedorJpaRepository.deleteAll();
        usuarioJpaRepository.deleteAll();
    }

    private Usuario criarUsuario(String email, Perfil perfil) {
        return ((com.example.Stq.autenticacao.domain.UsuarioRepository) usuarioJpaRepository)
                .save(Usuario.builder()
                        .nome(perfil.name()).email(email).senha("hash").perfil(perfil).ativo(true).build());
    }

    private RequestPostProcessor autenticar(Usuario usuario) {
        UsuarioDetails details = new UsuarioDetails(usuario);
        return authentication(new UsernamePasswordAuthenticationToken(
                details, null, details.getAuthorities()));
    }

    @Test
    @DisplayName("[PC-I1] fluxo RASCUNHO→PENDENTE→APROVADO→RECEBIDO deve incrementar estoque e gerar ENTRADA")
    void fluxoCompletoDeveIncrementarEstoque() throws Exception {
        var criar = new CriarPedidoRequest(fornecedorId, LocalDate.now(), null, "compra",
                List.of(new ItemRequest(produtoId, null, 7, new BigDecimal("10.00"))));

        String body = mockMvc.perform(post("/api/pedidos-compra")
                        .with(operador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(criar)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RASCUNHO"))
                .andExpect(jsonPath("$.totalPedido").value(70.00))
                .andReturn().getResponse().getContentAsString();
        UUID pedidoId = UUID.fromString(json.readTree(body).get("id").asText());

        mockMvc.perform(patch("/api/pedidos-compra/" + pedidoId + "/enviar").with(operador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE"));

        mockMvc.perform(patch("/api/pedidos-compra/" + pedidoId + "/aprovar").with(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"));

        mockMvc.perform(patch("/api/pedidos-compra/" + pedidoId + "/receber").with(operador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEBIDO"));

        assertThat(estoqueJpaRepository.findAll())
                .singleElement()
                .satisfies(e -> assertThat(e.getSaldoAtual()).isEqualTo(7));
        assertThat(movimentacaoJpaRepository.findAll())
                .singleElement()
                .satisfies(m -> assertThat(m.getOrigem()).isEqualTo(OrigemMovimentacao.PEDIDO_COMPRA));
    }

    @Test
    @DisplayName("[PC-I2] aprovar por OPERADOR deve retornar 403 (RN-10)")
    void aprovarPorOperadorDeveRetornar403() throws Exception {
        var criar = new CriarPedidoRequest(fornecedorId, LocalDate.now(), null, null,
                List.of(new ItemRequest(produtoId, null, 1, new BigDecimal("10.00"))));
        String body = mockMvc.perform(post("/api/pedidos-compra")
                        .with(operador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(criar)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID pedidoId = UUID.fromString(json.readTree(body).get("id").asText());

        mockMvc.perform(patch("/api/pedidos-compra/" + pedidoId + "/enviar").with(operador))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/pedidos-compra/" + pedidoId + "/aprovar").with(operador))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("[PC-I3] receber pedido não aprovado deve retornar 409 (RN-05)")
    void receberNaoAprovadoDeveRetornar409() throws Exception {
        var criar = new CriarPedidoRequest(fornecedorId, LocalDate.now(), null, null,
                List.of(new ItemRequest(produtoId, null, 1, new BigDecimal("10.00"))));
        String body = mockMvc.perform(post("/api/pedidos-compra")
                        .with(operador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(criar)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID pedidoId = UUID.fromString(json.readTree(body).get("id").asText());

        mockMvc.perform(patch("/api/pedidos-compra/" + pedidoId + "/receber").with(operador))
                .andExpect(status().isConflict());
    }
}
