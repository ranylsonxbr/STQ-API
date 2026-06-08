package com.example.Stq.produto;

import com.example.Stq.autenticacao.domain.Perfil;
import com.example.Stq.autenticacao.domain.Usuario;
import com.example.Stq.autenticacao.domain.UsuarioRepository;
import com.example.Stq.autenticacao.infra.UsuarioJpaRepository;
import com.example.Stq.fornecedor.domain.Fornecedor;
import com.example.Stq.fornecedor.infra.FornecedorJpaRepository;
import com.example.Stq.pedidocompra.domain.ItemPedidoCompra;
import com.example.Stq.pedidocompra.domain.PedidoCompra;
import com.example.Stq.pedidocompra.domain.StatusPedido;
import com.example.Stq.pedidocompra.infra.PedidoCompraJpaRepository;
import com.example.Stq.produto.application.dto.ProdutoCreateRequest;
import com.example.Stq.produto.application.dto.ProdutoUpdateRequest;
import com.example.Stq.produto.application.dto.VariacaoCreateRequest;
import com.example.Stq.produto.domain.Categoria;
import com.example.Stq.produto.domain.Produto;
import com.example.Stq.produto.domain.UnidadeMedida;
import com.example.Stq.produto.infra.CategoriaJpaRepository;
import com.example.Stq.produto.infra.ProdutoJpaRepository;
import com.example.Stq.produto.infra.VariacaoProdutoJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ProdutoIntegrationTest")
class ProdutoIntegrationTest {

    @Autowired WebApplicationContext wac;
    @Autowired CategoriaJpaRepository categoriaJpaRepository;
    @Autowired ProdutoJpaRepository produtoJpaRepository;
    @Autowired VariacaoProdutoJpaRepository variacaoProdutoJpaRepository;
    @Autowired PedidoCompraJpaRepository pedidoCompraJpaRepository;
    @Autowired FornecedorJpaRepository fornecedorJpaRepository;
    @Autowired UsuarioJpaRepository usuarioJpaRepository;

    private MockMvc mockMvc;
    private final ObjectMapper json = new ObjectMapper().registerModule(new JavaTimeModule());
    private Categoria categoriaAtiva;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
        pedidoCompraJpaRepository.deleteAll();
        variacaoProdutoJpaRepository.deleteAll();
        produtoJpaRepository.deleteAll();
        categoriaJpaRepository.deleteAll();
        fornecedorJpaRepository.deleteAll();
        usuarioJpaRepository.deleteAll();
        categoriaAtiva = categoriaJpaRepository.save(
                Categoria.builder().nome("Cat Teste").ativo(true).build());
    }

    private String criarProdutoERetornarId(String nome) throws Exception {
        var result = mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("op").roles("OPERADOR"))
                        .content(json.writeValueAsString(
                                new ProdutoCreateRequest(nome, null, categoriaAtiva.getId(), UnidadeMedida.UN, 0))))
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        return location.substring(location.lastIndexOf('/') + 1);
    }

    @Nested
    @DisplayName("POST /api/produtos")
    class Criar {

        @Test
        @WithMockUser(roles = "OPERADOR")
        @DisplayName("[PROD-C1] deve criar produto e retornar 201 com SKU gerado")
        void deveCriarProduto() throws Exception {
            mockMvc.perform(post("/api/produtos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(
                                    new ProdutoCreateRequest("Produto Integração", null, categoriaAtiva.getId(), UnidadeMedida.UN, 0))))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andExpect(jsonPath("$.sku").value(matchesRegex("PRD-[A-Z2-9]{6}")))
                    .andExpect(jsonPath("$.nome").value("Produto Integração"))
                    .andExpect(jsonPath("$.ativo").value(true));
        }

        @Test
        @WithMockUser(roles = "VISUALIZADOR")
        @DisplayName("deve retornar 403 para VISUALIZADOR")
        void deveRetornar403ParaVisualizador() throws Exception {
            mockMvc.perform(post("/api/produtos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(
                                    new ProdutoCreateRequest("Produto Negado", null, categoriaAtiva.getId(), UnidadeMedida.UN, 0))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "OPERADOR")
        @DisplayName("deve retornar 400 para nome curto")
        void deveRetornar400NomeCurto() throws Exception {
            mockMvc.perform(post("/api/produtos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(
                                    new ProdutoCreateRequest("AB", null, categoriaAtiva.getId(), UnidadeMedida.UN, 0))))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/produtos")
    class Listar {

        @Test
        @WithMockUser(roles = "VISUALIZADOR")
        @DisplayName("[PROD-C5] deve listar produtos paginados")
        void deveListarProdutos() throws Exception {
            criarProdutoERetornarId("Produto Lista A");
            criarProdutoERetornarId("Produto Lista B");

            mockMvc.perform(get("/api/produtos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        @DisplayName("deve retornar 401 sem autenticacao")
        void deveRetornar401SemAuth() throws Exception {
            mockMvc.perform(get("/api/produtos"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/produtos/{id}")
    class BuscarPorId {

        @Test
        @WithMockUser(roles = "VISUALIZADOR")
        @DisplayName("[PROD-C6] deve retornar produto com variacoes por ID")
        void deveBuscarPorId() throws Exception {
            String id = criarProdutoERetornarId("Produto BuscarId");

            mockMvc.perform(get("/api/produtos/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.variacoes").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/produtos/sku/{sku}")
    class BuscarPorSku {

        @Test
        @WithMockUser(roles = "VISUALIZADOR")
        @DisplayName("[PROD-C7] deve retornar produto por SKU")
        void deveBuscarPorSku() throws Exception {
            criarProdutoERetornarId("Produto BuscarSku");

            mockMvc.perform(get("/api/produtos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].sku").value(matchesRegex("PRD-[A-Z2-9]{6}")))
                    .andReturn();

            var listResult = mockMvc.perform(get("/api/produtos"))
                    .andReturn();
            String sku = new ObjectMapper().registerModule(new JavaTimeModule())
                    .readTree(listResult.getResponse().getContentAsString())
                    .at("/content/0/sku").asText();

            mockMvc.perform(get("/api/produtos/sku/{sku}", sku))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sku").value(sku));
        }
    }

    @Nested
    @DisplayName("PUT /api/produtos/{id}")
    class Atualizar {

        @Test
        @WithMockUser(roles = "OPERADOR")
        @DisplayName("[PROD-C8] deve atualizar produto sem alterar SKU")
        void deveAtualizarProduto() throws Exception {
            String id = criarProdutoERetornarId("Produto Antes");

            var skuResult = mockMvc.perform(get("/api/produtos/{id}", id)).andReturn();
            String skuOriginal = new ObjectMapper().registerModule(new JavaTimeModule())
                    .readTree(skuResult.getResponse().getContentAsString())
                    .at("/sku").asText();

            mockMvc.perform(put("/api/produtos/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(
                                    new ProdutoUpdateRequest("Produto Depois", null, categoriaAtiva.getId(), UnidadeMedida.KG, 5, null))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("Produto Depois"))
                    .andExpect(jsonPath("$.sku").value(skuOriginal));
        }
    }

    @Nested
    @DisplayName("DELETE /api/produtos/{id}")
    class Desativar {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("[PROD-C9] deve desativar produto")
        void deveDesativarProduto() throws Exception {
            String id = criarProdutoERetornarId("Produto Desativar");

            mockMvc.perform(delete("/api/produtos/{id}", id))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/produtos/{id}", id))
                    .andExpect(jsonPath("$.ativo").value(false));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("[PROD-I2] deve retornar 409 quando produto tem pedido em aberto (RN-07)")
        void deveRetornar409ComPedidoEmAberto() throws Exception {
            String produtoIdStr = criarProdutoERetornarId("Produto RN-07");
            Produto produto = produtoJpaRepository.findById(UUID.fromString(produtoIdStr)).orElseThrow();

            Fornecedor fornecedor = fornecedorJpaRepository.save(
                    Fornecedor.builder().razaoSocial("Forn RN07").cnpj("22333444000181").ativo(true).build());
            Usuario usuario = ((UsuarioRepository) usuarioJpaRepository)
                    .save(Usuario.builder().nome("admin").email("admin-rn07@test.com")
                            .senha("hash").perfil(Perfil.ADMIN).ativo(true).build());

            PedidoCompra pedido = PedidoCompra.builder()
                    .fornecedor(fornecedor).status(StatusPedido.PENDENTE)
                    .dataEmissao(LocalDate.now()).criadoPor(usuario)
                    .totalPedido(BigDecimal.TEN).build();
            ItemPedidoCompra item = ItemPedidoCompra.builder()
                    .produto(produto).quantidade(1)
                    .precoUnitario(BigDecimal.TEN).subtotal(BigDecimal.TEN).build();
            item.setPedidoCompra(pedido);
            pedido.getItens().add(item);
            pedidoCompraJpaRepository.save(pedido);

            mockMvc.perform(delete("/api/produtos/{id}", produtoIdStr))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("POST e DELETE /api/produtos/{id}/variacoes")
    class Variacoes {

        @Test
        @WithMockUser(roles = "OPERADOR")
        @DisplayName("[VAR-C1] deve adicionar variacao ao produto")
        void deveAdicionarVariacao() throws Exception {
            String produtoId = criarProdutoERetornarId("Produto com Variacao");

            mockMvc.perform(post("/api/produtos/{id}/variacoes", produtoId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(new VariacaoCreateRequest("Cor", "Vermelho"))))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andExpect(jsonPath("$.atributo").value("Cor"))
                    .andExpect(jsonPath("$.skuVariacao").value(matchesRegex("PRD-[A-Z2-9]{6}-[A-Z2-9]{3}")));
        }

        @Test
        @WithMockUser(roles = "OPERADOR")
        @DisplayName("[VAR-C3] deve retornar 409 para variacao duplicada")
        void deveRetornar409VariacaoDuplicada() throws Exception {
            String produtoId = criarProdutoERetornarId("Produto Dup Variacao");

            mockMvc.perform(post("/api/produtos/{id}/variacoes", produtoId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(new VariacaoCreateRequest("Cor", "Azul"))))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/produtos/{id}/variacoes", produtoId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(new VariacaoCreateRequest("Cor", "Azul"))))
                    .andExpect(status().isConflict());
        }

        @Test
        @WithMockUser(roles = "OPERADOR")
        @DisplayName("[VAR-C4] deve desativar variacao")
        void deveDesativarVariacao() throws Exception {
            String produtoId = criarProdutoERetornarId("Produto Desativar Variacao");

            var result = mockMvc.perform(post("/api/produtos/{id}/variacoes", produtoId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(new VariacaoCreateRequest("Tamanho", "M"))))
                    .andExpect(status().isCreated())
                    .andReturn();

            String location = result.getResponse().getHeader("Location");
            String variacaoId = location.substring(location.lastIndexOf('/') + 1);

            mockMvc.perform(delete("/api/produtos/{id}/variacoes/{vid}", produtoId, variacaoId))
                    .andExpect(status().isNoContent());
        }
    }
}
