package com.example.Stq.frontend.web;

import com.example.Stq.produto.application.dto.CategoriaResponse;
import com.example.Stq.produto.application.dto.ProdutoDetalheResponse;
import com.example.Stq.produto.application.dto.ProdutoResponse;
import com.example.Stq.produto.application.services.CategoriaService;
import com.example.Stq.produto.application.services.ProdutoService;
import com.example.Stq.produto.application.services.VariacaoProdutoService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProdutoWebController")
class ProdutoWebControllerTest {

    @Mock
    private ProdutoService produtoService;
    @Mock
    private CategoriaService categoriaService;
    @Mock
    private VariacaoProdutoService variacaoProdutoService;

    @InjectMocks
    private ProdutoWebController controller;

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

    private ProdutoResponse produtoResponse(UUID id) {
        return new ProdutoResponse(id, "PRD-000001", "Produto Teste", "Descricao",
                randomId(), "Categoria Teste", UnidadeMedida.UN, 5, true, Instant.now());
    }

    private ProdutoDetalheResponse produtoDetalhe(UUID id) {
        return new ProdutoDetalheResponse(id, "PRD-000001", "Produto Teste", "Descricao",
                randomId(), "Categoria Teste", UnidadeMedida.UN, 5, true, Instant.now(), List.of());
    }

    private Page<CategoriaResponse> paginaCategorias() {
        var cat = new CategoriaResponse(randomId(), "Cat A", null, true);
        return new PageImpl<>(List.of(cat));
    }

    private void mockCategorias() {
        when(categoriaService.listar(any(), any(), any())).thenReturn(paginaCategorias());
    }

    // -------------------------------------------------------------------------
    // GET /web/produtos
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /web/produtos")
    class Listar {

        @Test
        @DisplayName("[FW-P1] deve retornar view 'produto/lista' com atributo 'produtos'")
        void deveRetornarListaComProdutos() throws Exception {
            // Arrange
            UUID id = randomId();
            Page<ProdutoResponse> page = new PageImpl<>(List.of(produtoResponse(id)));
            when(produtoService.listar(any(), any(), any(), any())).thenReturn(page);
            mockCategorias();

            // Act + Assert
            mockMvc.perform(get("/web/produtos"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("produto/lista"))
                    .andExpect(model().attributeExists("produtos"))
                    .andExpect(model().attributeExists("categorias"));
        }

        @Test
        @DisplayName("[FW-P2] deve repassar filtros ao model quando fornecidos")
        void deveRepassarFiltrosAoModel() throws Exception {
            // Arrange
            UUID catId = randomId();
            Page<ProdutoResponse> page = new PageImpl<>(List.of());
            when(produtoService.listar(any(), any(), any(), any())).thenReturn(page);
            mockCategorias();

            // Act + Assert
            mockMvc.perform(get("/web/produtos")
                            .param("nome", "Teste")
                            .param("categoriaId", catId.toString())
                            .param("ativo", "true"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("filtroNome", "Teste"))
                    .andExpect(model().attribute("filtroAtivo", true));
        }
    }

    // -------------------------------------------------------------------------
    // GET /web/produtos/{id}
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /web/produtos/{id}")
    class Detalhe {

        @Test
        @DisplayName("[FW-P3] deve retornar view 'produto/detalhe' com atributo 'produto'")
        void deveRetornarDetalhe() throws Exception {
            // Arrange
            UUID id = randomId();
            when(produtoService.buscarPorId(id)).thenReturn(produtoDetalhe(id));

            // Act + Assert
            mockMvc.perform(get("/web/produtos/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(view().name("produto/detalhe"))
                    .andExpect(model().attributeExists("produto"));
        }
    }

    // -------------------------------------------------------------------------
    // GET /web/produtos/novo
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /web/produtos/novo")
    class NovoForm {

        @Test
        @DisplayName("[FW-P4] deve retornar view 'produto/form' com produtoForm e listas de apoio")
        void deveRetornarFormNovo() throws Exception {
            // Arrange
            mockCategorias();

            // Act + Assert
            mockMvc.perform(get("/web/produtos/novo"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("produto/form"))
                    .andExpect(model().attributeExists("produtoForm"))
                    .andExpect(model().attributeExists("categorias"))
                    .andExpect(model().attributeExists("unidades"))
                    .andExpect(model().attribute("editando", false));
        }
    }

    // -------------------------------------------------------------------------
    // POST /web/produtos
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /web/produtos")
    class Criar {

        @Test
        @DisplayName("[FW-P5] deve redirecionar para detalhe com flash sucesso apos criacao valida")
        void deveRedirecionarComFlashSucesso() throws Exception {
            // Arrange — categoria NOT stubbed: controller redirects without touching categoriaService
            UUID id = randomId();
            when(produtoService.criar(any())).thenReturn(produtoResponse(id));

            // Act + Assert
            mockMvc.perform(post("/web/produtos")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("nome", "Produto Novo")
                            .param("descricao", "Descricao")
                            .param("categoriaId", randomId().toString())
                            .param("unidadeMedida", "UN")
                            .param("estoqueMinimo", "5"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("/web/produtos/*"))
                    .andExpect(flash().attribute("sucesso", "Produto criado com sucesso."));

            verify(produtoService).criar(any());
        }

        @Test
        @DisplayName("[FW-P6] deve voltar ao form com erros quando dados invalidos")
        void deveRetornarFormComErros() throws Exception {
            // Arrange
            mockCategorias();

            // Act + Assert — nome em branco dispara @NotBlank
            mockMvc.perform(post("/web/produtos")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("nome", "")
                            .param("categoriaId", randomId().toString())
                            .param("unidadeMedida", "UN")
                            .param("estoqueMinimo", "0"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("produto/form"))
                    .andExpect(model().attributeExists("categorias"))
                    .andExpect(model().attribute("editando", false));
        }
    }

    // -------------------------------------------------------------------------
    // GET /web/produtos/{id}/editar
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /web/produtos/{id}/editar")
    class EditarForm {

        @Test
        @DisplayName("[FW-P7] deve retornar view 'produto/form' em modo edicao")
        void deveRetornarFormEdicao() throws Exception {
            // Arrange
            UUID id = randomId();
            when(produtoService.buscarPorId(id)).thenReturn(produtoDetalhe(id));
            mockCategorias();

            // Act + Assert
            mockMvc.perform(get("/web/produtos/{id}/editar", id))
                    .andExpect(status().isOk())
                    .andExpect(view().name("produto/form"))
                    .andExpect(model().attributeExists("produtoForm"))
                    .andExpect(model().attribute("editando", true))
                    .andExpect(model().attribute("produtoId", id));
        }
    }

    // -------------------------------------------------------------------------
    // POST /web/produtos/{id}
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /web/produtos/{id}")
    class Atualizar {

        @Test
        @DisplayName("[FW-P8] deve redirecionar para detalhe com flash sucesso apos atualizacao valida")
        void deveRedirecionarComFlashSucessoAposAtualizacao() throws Exception {
            // Arrange — categoria NOT stubbed: controller redirects without touching categoriaService
            UUID id = randomId();
            when(produtoService.atualizar(eq(id), any())).thenReturn(produtoResponse(id));

            // Act + Assert
            mockMvc.perform(post("/web/produtos/{id}", id)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("nome", "Produto Atualizado")
                            .param("descricao", "Nova desc")
                            .param("categoriaId", randomId().toString())
                            .param("unidadeMedida", "KG")
                            .param("estoqueMinimo", "10"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/produtos/" + id))
                    .andExpect(flash().attribute("sucesso", "Produto atualizado com sucesso."));

            verify(produtoService).atualizar(eq(id), any());
        }

        @Test
        @DisplayName("[FW-P9] deve voltar ao form com erros quando dados invalidos na atualizacao")
        void deveRetornarFormComErrosNaAtualizacao() throws Exception {
            // Arrange
            UUID id = randomId();
            mockCategorias();

            // Act + Assert — nome muito curto (<3 chars)
            mockMvc.perform(post("/web/produtos/{id}", id)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("nome", "AB")
                            .param("categoriaId", randomId().toString())
                            .param("unidadeMedida", "UN")
                            .param("estoqueMinimo", "0"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("produto/form"))
                    .andExpect(model().attribute("editando", true));
        }
    }

    // -------------------------------------------------------------------------
    // POST /web/produtos/{id}/desativar
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /web/produtos/{id}/desativar")
    class Desativar {

        @Test
        @DisplayName("[FW-P10] deve redirecionar para lista com flash sucesso ao desativar")
        void deveRedirecionarComFlashSucessoAoDesativar() throws Exception {
            // Arrange
            UUID id = randomId();
            doNothing().when(produtoService).desativar(id);

            // Act + Assert
            mockMvc.perform(post("/web/produtos/{id}/desativar", id))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/produtos"))
                    .andExpect(flash().attribute("sucesso", "Produto desativado com sucesso."));

            verify(produtoService).desativar(id);
        }

        @Test
        @DisplayName("[FW-P11] deve redirecionar com flash erro quando RuntimeException (RN-07)")
        void deveRedirecionarComFlashErroQuandoRuntimeException() throws Exception {
            // Arrange
            UUID id = randomId();
            doThrow(new RuntimeException("Produto possui pedido em aberto."))
                    .when(produtoService).desativar(id);

            // Act + Assert
            mockMvc.perform(post("/web/produtos/{id}/desativar", id))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/produtos"))
                    .andExpect(flash().attribute("erro", "Produto possui pedido em aberto."));
        }
    }

    // -------------------------------------------------------------------------
    // GET /web/produtos/categorias
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /web/produtos/categorias")
    class ListarCategorias {

        @Test
        @DisplayName("[FW-P12] deve retornar view 'produto/categorias' com lista de categorias")
        void deveRetornarListaCategorias() throws Exception {
            // Arrange
            mockCategorias();

            // Act + Assert
            mockMvc.perform(get("/web/produtos/categorias"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("produto/categorias"))
                    .andExpect(model().attributeExists("categorias"))
                    .andExpect(model().attributeExists("categoriaForm"));
        }
    }

    // -------------------------------------------------------------------------
    // POST /web/produtos/categorias
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /web/produtos/categorias")
    class CriarCategoria {

        @Test
        @DisplayName("[FW-P13] deve redirecionar com flash sucesso ao criar categoria valida")
        void deveRedirecionarComFlashSucessoAoCriarCategoria() throws Exception {
            // Arrange — listar NOT stubbed: controller redirects without re-fetching list
            var catResponse = new CategoriaResponse(randomId(), "Nova Cat", null, true);
            when(categoriaService.criar(any())).thenReturn(catResponse);

            // Act + Assert
            mockMvc.perform(post("/web/produtos/categorias")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("nome", "Nova Categoria"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/produtos/categorias"))
                    .andExpect(flash().attribute("sucesso", "Categoria criada com sucesso."));
        }

        @Test
        @DisplayName("[FW-P14] deve retornar form de categorias com erros quando nome invalido")
        void deveRetornarFormComErrosQuandoNomeInvalido() throws Exception {
            // Arrange
            mockCategorias();

            // Act + Assert — nome em branco
            mockMvc.perform(post("/web/produtos/categorias")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("nome", ""))
                    .andExpect(status().isOk())
                    .andExpect(view().name("produto/categorias"))
                    .andExpect(model().attributeExists("categorias"));
        }
    }

    // -------------------------------------------------------------------------
    // POST /web/produtos/{id}/variacoes
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /web/produtos/{id}/variacoes")
    class AdicionarVariacao {

        @Test
        @DisplayName("[FW-P15] deve redirecionar para detalhe com flash sucesso ao adicionar variacao")
        void deveRedirecionarComFlashSucessoAoAdicionarVariacao() throws Exception {
            // Arrange
            UUID id = randomId();

            // Act + Assert
            mockMvc.perform(post("/web/produtos/{id}/variacoes", id)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("atributo", "Cor")
                            .param("valor", "Azul"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/produtos/" + id))
                    .andExpect(flash().attribute("sucesso", "Variação adicionada com sucesso."));

            verify(variacaoProdutoService).adicionar(eq(id), any());
        }
    }
}
