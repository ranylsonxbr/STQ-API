package com.example.Stq.relatorio;

import com.example.Stq.autenticacao.domain.Perfil;
import com.example.Stq.autenticacao.domain.Usuario;
import com.example.Stq.autenticacao.infra.UsuarioDetails;
import com.example.Stq.autenticacao.infra.UsuarioJpaRepository;
import com.example.Stq.movimentacao.application.dto.EntradaRequest;
import com.example.Stq.movimentacao.infra.EstoqueJpaRepository;
import com.example.Stq.movimentacao.infra.MovimentacaoJpaRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("RelatorioIntegrationTest")
class RelatorioIntegrationTest {

    @Autowired WebApplicationContext wac;
    @Autowired CategoriaJpaRepository categoriaJpaRepository;
    @Autowired ProdutoJpaRepository produtoJpaRepository;
    @Autowired VariacaoProdutoJpaRepository variacaoProdutoJpaRepository;
    @Autowired EstoqueJpaRepository estoqueJpaRepository;
    @Autowired MovimentacaoJpaRepository movimentacaoJpaRepository;
    @Autowired UsuarioJpaRepository usuarioJpaRepository;

    private MockMvc mockMvc;
    private final ObjectMapper json = new ObjectMapper().registerModule(new JavaTimeModule());
    private java.util.UUID produtoId;
    private RequestPostProcessor operador;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
        limpar();

        Categoria categoria = categoriaJpaRepository.save(
                Categoria.builder().nome("Cat Relatorio").ativo(true).build());
        Produto produto = produtoJpaRepository.save(Produto.builder()
                .sku("PRD-REL001").nome("Produto Relatorio").categoria(categoria)
                .unidadeMedida(UnidadeMedida.UN).estoqueMinimo(5).ativo(true).build());
        produtoId = produto.getId();
        operador = autenticar(criarUsuario("operador@relatorio.com", Perfil.OPERADOR));
    }

    @AfterEach
    void tearDown() {
        limpar();
    }

    private void limpar() {
        movimentacaoJpaRepository.deleteAll();
        estoqueJpaRepository.deleteAll();
        variacaoProdutoJpaRepository.deleteAll();
        produtoJpaRepository.deleteAll();
        categoriaJpaRepository.deleteAll();
        usuarioJpaRepository.deleteAll();
    }

    private Usuario criarUsuario(String email, Perfil perfil) {
        return ((com.example.Stq.autenticacao.domain.UsuarioRepository) usuarioJpaRepository)
                .save(Usuario.builder().nome(perfil.name()).email(email)
                        .senha("hash").perfil(perfil).ativo(true).build());
    }

    private RequestPostProcessor autenticar(Usuario usuario) {
        UsuarioDetails details = new UsuarioDetails(usuario);
        return authentication(new UsernamePasswordAuthenticationToken(
                details, null, details.getAuthorities()));
    }

    @Test
    @DisplayName("[REL-I1] estoque-atual deve refletir saldo real após entrada")
    void estoqueAtualDeveRefletirSaldoReal() throws Exception {
        mockMvc.perform(post("/api/movimentacoes/entrada")
                        .with(operador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(
                                new EntradaRequest(produtoId, null, "A1", 10, null))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/relatorios/estoque-atual").with(operador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("PRD-REL001"))
                .andExpect(jsonPath("$[0].saldoAtual").value(10))
                .andExpect(jsonPath("$[0].status").value("NORMAL"));
    }

    @Test
    @DisplayName("[REL-I2] alertas deve retornar produto com saldo abaixo do mínimo")
    void alertasDeveRetornarProdutoAbaixoDoMinimo() throws Exception {
        mockMvc.perform(post("/api/movimentacoes/entrada")
                        .with(operador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(
                                new EntradaRequest(produtoId, null, "A1", 3, null))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/relatorios/alertas").with(operador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("PRD-REL001"))
                .andExpect(jsonPath("$[0].status").value("ABAIXO_MINIMO"));
    }

    @Test
    @DisplayName("[REL-I3] alertas deve ser vazio quando estoque está normal")
    void alertasVazioQuandoEstoqueNormal() throws Exception {
        mockMvc.perform(post("/api/movimentacoes/entrada")
                        .with(operador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(
                                new EntradaRequest(produtoId, null, "A1", 10, null))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/relatorios/alertas").with(operador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("[REL-I4] CSV de estoque-atual deve conter cabeçalho e dados")
    void csvEstoqueAtualDeveConterCabecalhoEDados() throws Exception {
        mockMvc.perform(post("/api/movimentacoes/entrada")
                        .with(operador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(
                                new EntradaRequest(produtoId, null, "A1", 8, null))))
                .andExpect(status().isCreated());

        var result = mockMvc.perform(get("/api/relatorios/estoque-atual")
                        .with(operador)
                        .header("Accept", "text/csv"))
                .andExpect(status().isOk())
                .andReturn();

        String csv = result.getResponse().getContentAsString();
        assertThat(csv).contains("SKU").contains("PRD-REL001").contains("NORMAL");
    }
}
