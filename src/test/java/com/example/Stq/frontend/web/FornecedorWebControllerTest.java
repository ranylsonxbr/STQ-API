package com.example.Stq.frontend.web;

import com.example.Stq.autenticacao.domain.Perfil;
import com.example.Stq.fornecedor.application.dto.FornecedorResponse;
import com.example.Stq.fornecedor.application.services.FornecedorService;
import com.example.Stq.frontend.support.UsuarioLogado;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
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
@DisplayName("FornecedorWebController")
class FornecedorWebControllerTest {

    @Mock
    private FornecedorService fornecedorService;
    @Mock
    private UsuarioLogado usuarioLogado;

    @InjectMocks
    private FornecedorWebController controller;

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

    private FornecedorResponse fornecedorResponse(UUID id) {
        return new FornecedorResponse(id, "Fornecedor Ltda", "11222333000181",
                "forn@teste.com", "11999990000", "Contato", true,
                Instant.now(), Instant.now(), randomId());
    }

    private Authentication authMock(UUID usuarioId) {
        Authentication auth = mock(Authentication.class);
        when(usuarioLogado.obterUsuarioId(auth)).thenReturn(usuarioId);
        return auth;
    }

    private Authentication authMockComPerfil(UUID usuarioId, Perfil perfil) {
        Authentication auth = mock(Authentication.class);
        when(usuarioLogado.obterUsuarioId(auth)).thenReturn(usuarioId);
        when(usuarioLogado.obterPerfil(auth)).thenReturn(perfil);
        return auth;
    }

    // -------------------------------------------------------------------------
    // GET /web/fornecedores
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /web/fornecedores")
    class Listar {

        @Test
        @DisplayName("[FW-F1] deve retornar view 'fornecedor/lista' com atributo 'fornecedores'")
        void deveRetornarListaComFornecedores() throws Exception {
            // Arrange
            UUID id = randomId();
            Page<FornecedorResponse> page = new PageImpl<>(List.of(fornecedorResponse(id)));
            when(fornecedorService.listar(any(), any())).thenReturn(page);

            // Act + Assert
            mockMvc.perform(get("/web/fornecedores"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("fornecedor/lista"))
                    .andExpect(model().attributeExists("fornecedores"));
        }

        @Test
        @DisplayName("[FW-F2] deve repassar filtros ao model quando fornecidos")
        void deveRepassarFiltrosAoModel() throws Exception {
            // Arrange
            Page<FornecedorResponse> page = new PageImpl<>(List.of());
            when(fornecedorService.listar(any(), any())).thenReturn(page);

            // Act + Assert
            mockMvc.perform(get("/web/fornecedores")
                            .param("razaoSocial", "Fornecedor X")
                            .param("cnpj", "11222333000181")
                            .param("ativo", "true"))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("filtroRazaoSocial", "Fornecedor X"))
                    .andExpect(model().attribute("filtroCnpj", "11222333000181"))
                    .andExpect(model().attribute("filtroAtivo", true));
        }
    }

    // -------------------------------------------------------------------------
    // GET /web/fornecedores/{id}
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /web/fornecedores/{id}")
    class Detalhe {

        @Test
        @DisplayName("[FW-F3] deve retornar view 'fornecedor/detalhe' com atributo 'fornecedor'")
        void deveRetornarDetalhe() throws Exception {
            // Arrange
            UUID id = randomId();
            when(fornecedorService.buscarPorId(id)).thenReturn(fornecedorResponse(id));

            // Act + Assert
            mockMvc.perform(get("/web/fornecedores/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(view().name("fornecedor/detalhe"))
                    .andExpect(model().attributeExists("fornecedor"));
        }
    }

    // -------------------------------------------------------------------------
    // GET /web/fornecedores/novo
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /web/fornecedores/novo")
    class NovoForm {

        @Test
        @DisplayName("[FW-F4] deve retornar view 'fornecedor/form' com fornecedorForm vazio")
        void deveRetornarFormNovo() throws Exception {
            mockMvc.perform(get("/web/fornecedores/novo"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("fornecedor/form"))
                    .andExpect(model().attributeExists("fornecedorForm"))
                    .andExpect(model().attribute("editando", false));
        }
    }

    // -------------------------------------------------------------------------
    // POST /web/fornecedores
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /web/fornecedores")
    class Criar {

        @Test
        @DisplayName("[FW-F5] deve redirecionar para detalhe com flash sucesso apos criacao valida")
        void deveRedirecionarComFlashSucessoAposCriacao() throws Exception {
            // Arrange
            UUID id = randomId();
            UUID usuarioId = randomId();
            Authentication auth = authMock(usuarioId);
            when(fornecedorService.criar(any(), eq(usuarioId))).thenReturn(fornecedorResponse(id));

            // Act + Assert
            mockMvc.perform(post("/web/fornecedores")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("razaoSocial", "Fornecedor Ltda")
                            .param("cnpj", "11222333000181")
                            .param("email", "forn@teste.com")
                            .param("telefone", "11999990000")
                            .param("contato", "Contato")
                            .principal(auth))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("/web/fornecedores/*"))
                    .andExpect(flash().attribute("sucesso", "Fornecedor criado com sucesso."));

            verify(fornecedorService).criar(any(), eq(usuarioId));
        }

        @Test
        @DisplayName("[FW-F6] deve retornar form com erros quando razaoSocial invalida")
        void deveRetornarFormComErrosQuandoDadosInvalidos() throws Exception {
            mockMvc.perform(post("/web/fornecedores")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("razaoSocial", "")
                            .param("cnpj", "")
                            .param("email", "nao-e-email"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("fornecedor/form"))
                    .andExpect(model().attribute("editando", false));
        }
    }

    // -------------------------------------------------------------------------
    // GET /web/fornecedores/{id}/editar
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /web/fornecedores/{id}/editar")
    class EditarForm {

        @Test
        @DisplayName("[FW-F7] deve retornar view 'fornecedor/form' em modo edicao")
        void deveRetornarFormEdicao() throws Exception {
            // Arrange
            UUID id = randomId();
            when(fornecedorService.buscarPorId(id)).thenReturn(fornecedorResponse(id));

            // Act + Assert
            mockMvc.perform(get("/web/fornecedores/{id}/editar", id))
                    .andExpect(status().isOk())
                    .andExpect(view().name("fornecedor/form"))
                    .andExpect(model().attributeExists("fornecedorForm"))
                    .andExpect(model().attribute("editando", true))
                    .andExpect(model().attribute("fornecedorId", id));
        }
    }

    // -------------------------------------------------------------------------
    // POST /web/fornecedores/{id}
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /web/fornecedores/{id}")
    class Atualizar {

        @Test
        @DisplayName("[FW-F8] deve redirecionar para detalhe com flash sucesso apos atualizacao valida")
        void deveRedirecionarComFlashSucessoAposAtualizacao() throws Exception {
            // Arrange
            UUID id = randomId();
            UUID usuarioId = randomId();
            Authentication auth = authMockComPerfil(usuarioId, Perfil.ADMIN);
            when(fornecedorService.atualizar(eq(id), any(), eq(usuarioId), eq(Perfil.ADMIN)))
                    .thenReturn(fornecedorResponse(id));

            // Act + Assert
            mockMvc.perform(post("/web/fornecedores/{id}", id)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("razaoSocial", "Fornecedor Atualizado")
                            .param("cnpj", "11222333000181")
                            .param("email", "novo@teste.com")
                            .param("telefone", "11888880000")
                            .param("contato", "Novo Contato")
                            .principal(auth))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/fornecedores/" + id))
                    .andExpect(flash().attribute("sucesso", "Fornecedor atualizado com sucesso."));
        }

        @Test
        @DisplayName("[FW-F9] deve retornar form com erros quando dados invalidos na atualizacao")
        void deveRetornarFormComErrosNaAtualizacao() throws Exception {
            // Arrange
            UUID id = randomId();

            // Act + Assert
            mockMvc.perform(post("/web/fornecedores/{id}", id)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("razaoSocial", "")
                            .param("cnpj", ""))
                    .andExpect(status().isOk())
                    .andExpect(view().name("fornecedor/form"))
                    .andExpect(model().attribute("editando", true))
                    .andExpect(model().attribute("fornecedorId", id));
        }
    }

    // -------------------------------------------------------------------------
    // POST /web/fornecedores/{id}/desativar
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /web/fornecedores/{id}/desativar")
    class Desativar {

        @Test
        @DisplayName("[FW-F10] deve redirecionar para lista com flash sucesso ao desativar")
        void deveRedirecionarComFlashSucesso() throws Exception {
            // Arrange
            UUID id = randomId();
            UUID usuarioId = randomId();
            Authentication auth = authMock(usuarioId);
            doNothing().when(fornecedorService).desativar(eq(id), eq(usuarioId));

            // Act + Assert
            mockMvc.perform(post("/web/fornecedores/{id}/desativar", id)
                            .principal(auth))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/fornecedores"))
                    .andExpect(flash().attribute("sucesso", "Fornecedor desativado com sucesso."));

            verify(fornecedorService).desativar(eq(id), eq(usuarioId));
        }

        @Test
        @DisplayName("[FW-F11] deve redirecionar com flash erro quando RuntimeException (RN-08)")
        void deveRedirecionarComFlashErroQuandoRuntimeException() throws Exception {
            // Arrange
            UUID id = randomId();
            UUID usuarioId = randomId();
            Authentication auth = authMock(usuarioId);
            doThrow(new RuntimeException("Fornecedor possui pedido em aberto."))
                    .when(fornecedorService).desativar(eq(id), eq(usuarioId));

            // Act + Assert
            mockMvc.perform(post("/web/fornecedores/{id}/desativar", id)
                            .principal(auth))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/fornecedores"))
                    .andExpect(flash().attribute("erro", "Fornecedor possui pedido em aberto."));
        }
    }

    // -------------------------------------------------------------------------
    // POST /web/fornecedores/{id}/reativar
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /web/fornecedores/{id}/reativar")
    class Reativar {

        @Test
        @DisplayName("[FW-F12] deve redirecionar para detalhe com flash sucesso ao reativar")
        void deveRedirecionarComFlashSucessoAoReativar() throws Exception {
            // Arrange
            UUID id = randomId();
            UUID usuarioId = randomId();
            Authentication auth = authMock(usuarioId);
            when(fornecedorService.reativar(eq(id), eq(usuarioId))).thenReturn(fornecedorResponse(id));

            // Act + Assert
            mockMvc.perform(post("/web/fornecedores/{id}/reativar", id)
                            .principal(auth))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/fornecedores/" + id))
                    .andExpect(flash().attribute("sucesso", "Fornecedor reativado com sucesso."));

            verify(fornecedorService).reativar(eq(id), eq(usuarioId));
        }
    }
}
