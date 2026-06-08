package com.example.Stq.movimentacao;

import com.example.Stq.autenticacao.domain.Perfil;
import com.example.Stq.autenticacao.domain.Usuario;
import com.example.Stq.autenticacao.infra.UsuarioDetails;
import com.example.Stq.autenticacao.infra.UsuarioJpaRepository;
import com.example.Stq.movimentacao.application.dto.AjusteRequest;
import com.example.Stq.movimentacao.application.dto.EntradaRequest;
import com.example.Stq.movimentacao.application.dto.SaidaRequest;
import com.example.Stq.movimentacao.application.dto.TransferenciaRequest;
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
import org.junit.jupiter.api.Nested;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("MovimentacaoIntegrationTest")
class MovimentacaoIntegrationTest {

    @Autowired WebApplicationContext wac;
    @Autowired CategoriaJpaRepository categoriaJpaRepository;
    @Autowired ProdutoJpaRepository produtoJpaRepository;
    @Autowired VariacaoProdutoJpaRepository variacaoProdutoJpaRepository;
    @Autowired EstoqueJpaRepository estoqueJpaRepository;
    @Autowired MovimentacaoJpaRepository movimentacaoJpaRepository;
    @Autowired UsuarioJpaRepository usuarioJpaRepository;

    private MockMvc mockMvc;
    private final ObjectMapper json = new ObjectMapper().registerModule(new JavaTimeModule());
    private UUID produtoId;
    private RequestPostProcessor operador;
    private RequestPostProcessor admin;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
        movimentacaoJpaRepository.deleteAll();
        estoqueJpaRepository.deleteAll();
        variacaoProdutoJpaRepository.deleteAll();
        produtoJpaRepository.deleteAll();
        categoriaJpaRepository.deleteAll();
        usuarioJpaRepository.deleteAll();

        Categoria categoria = categoriaJpaRepository.save(
                Categoria.builder().nome("Cat Movimentacao").ativo(true).build());
        Produto produto = produtoJpaRepository.save(Produto.builder()
                .sku("PRD-MOV001")
                .nome("Produto Movimentacao")
                .categoria(categoria)
                .unidadeMedida(UnidadeMedida.UN)
                .estoqueMinimo(5)
                .ativo(true)
                .build());
        produtoId = produto.getId();

        operador = autenticar(criarUsuario("operador@teste.com", Perfil.OPERADOR));
        admin = autenticar(criarUsuario("admin@teste.com", Perfil.ADMIN));
    }

    @AfterEach
    void tearDown() {
        movimentacaoJpaRepository.deleteAll();
        estoqueJpaRepository.deleteAll();
        produtoJpaRepository.deleteAll();
        categoriaJpaRepository.deleteAll();
        usuarioJpaRepository.deleteAll();
    }

    private Usuario criarUsuario(String email, Perfil perfil) {
        return ((com.example.Stq.autenticacao.domain.UsuarioRepository) usuarioJpaRepository)
                .save(Usuario.builder()
                        .nome(perfil.name())
                        .email(email)
                        .senha("hash")
                        .perfil(perfil)
                        .ativo(true)
                        .build());
    }

    private RequestPostProcessor autenticar(Usuario usuario) {
        UsuarioDetails details = new UsuarioDetails(usuario);
        return authentication(new UsernamePasswordAuthenticationToken(
                details, null, details.getAuthorities()));
    }

    @Nested
    @DisplayName("[MOV-I1] fluxo entrada → consulta de saldo")
    class FluxoEntradaConsulta {

        @Test
        @DisplayName("[MOV-I1] entrada deve refletir saldo e status calculado em GET /api/estoques")
        void entradaDeveRefletirSaldoEStatus() throws Exception {
            mockMvc.perform(post("/api/movimentacoes/entrada")
                            .with(operador)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(
                                    new EntradaRequest(produtoId, null, "A1", 10, "compra inicial"))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.saldoDepois").value(10));

            mockMvc.perform(get("/api/estoques")
                            .param("produtoId", produtoId.toString())
                            .with(operador))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].saldoAtual").value(10))
                    .andExpect(jsonPath("$.content[0].status").value("NORMAL"));
        }
    }

    @Nested
    @DisplayName("[MOV-I2] saída insuficiente com rollback")
    class SaidaInsuficiente {

        @Test
        @DisplayName("[MOV-I2] saída maior que saldo deve retornar 422 e não alterar saldo")
        void saidaInsuficienteDeveRetornar422SemAlterarSaldo() throws Exception {
            mockMvc.perform(post("/api/movimentacoes/entrada")
                            .with(operador)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(
                                    new EntradaRequest(produtoId, null, "A1", 5, null))))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/movimentacoes/saida")
                            .with(operador)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(
                                    new SaidaRequest(produtoId, null, "A1", 10, null))))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422));

            mockMvc.perform(get("/api/estoques")
                            .param("produtoId", produtoId.toString())
                            .with(operador))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].saldoAtual").value(5));
        }
    }

    @Nested
    @DisplayName("[MOV-I3] transferência atômica")
    class TransferenciaAtomica {

        @Test
        @DisplayName("[MOV-I3] transferência deve decrementar origem e incrementar destino")
        void transferenciaDeveMoverSaldoEntreLocalizacoes() throws Exception {
            mockMvc.perform(post("/api/movimentacoes/entrada")
                            .with(operador)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(
                                    new EntradaRequest(produtoId, null, "A1", 10, null))))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/movimentacoes/transferencia")
                            .with(operador)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(
                                    new TransferenciaRequest(produtoId, null, "A1", "B2", 4, null))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.tipo").value("TRANSFERENCIA"));

            mockMvc.perform(get("/api/estoques")
                            .param("produtoId", produtoId.toString())
                            .with(operador))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2));

            assertThat(estoqueJpaRepository.count()).isEqualTo(2);
            assertThat(movimentacaoJpaRepository.count()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("[MOV-I4] segurança do ajuste")
    class SegurancaAjuste {

        @Test
        @DisplayName("[MOV-I4] ajuste por OPERADOR deve retornar 403 (RN-09)")
        void ajustePorOperadorDeveRetornar403() throws Exception {
            mockMvc.perform(post("/api/movimentacoes/ajuste")
                            .with(operador)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(
                                    new AjusteRequest(produtoId, null, "A1", 5, null))))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("[MOV-I5] ajuste ADMIN com saldo negativo")
    class AjusteSaldoNegativo {

        @Test
        @DisplayName("[MOV-I5] ajuste por ADMIN com delta negativo deve permitir saldo negativo")
        void ajusteAdminComDeltaNegativoDevePermitirSaldoNegativo() throws Exception {
            mockMvc.perform(post("/api/movimentacoes/entrada")
                            .with(operador)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(
                                    new EntradaRequest(produtoId, null, "A1", 3, null))))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/movimentacoes/ajuste")
                            .with(admin)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(
                                    new AjusteRequest(produtoId, null, "A1", -5, "contagem"))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.saldoDepois").value(-2))
                    .andExpect(jsonPath("$.quantidade").value(5));
        }
    }

    @Nested
    @DisplayName("[MOV-I6] imutabilidade REST")
    class ImutabilidadeRest {

        @Test
        @DisplayName("[MOV-I6] PUT em /api/movimentacoes deve retornar 405 (RN-02/US-07)")
        void putEmMovimentacoesDeveRetornar405() throws Exception {
            mockMvc.perform(put("/api/movimentacoes")
                            .with(operador)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isMethodNotAllowed());
        }
    }
}
