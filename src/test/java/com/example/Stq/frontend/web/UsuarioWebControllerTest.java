package com.example.Stq.frontend.web;

import com.example.Stq.autenticacao.application.dto.UsuarioResponse;
import com.example.Stq.autenticacao.application.services.UsuarioService;
import com.example.Stq.autenticacao.domain.Perfil;
import com.example.Stq.autenticacao.domain.exception.EmailJaCadastradoException;
import com.example.Stq.autenticacao.domain.exception.OperacaoNegadaException;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Testes unitários do UsuarioWebController com standaloneSetup (sem Spring Security).
 *
 * Cenários de controle de acesso por role (ADMIN vs OPERADOR) são cobertos pelo
 * FrontendIntegrationTest, que sobe o contexto completo com Spring Security.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioWebController")
class UsuarioWebControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private UsuarioLogado usuarioLogado;

    @InjectMocks
    private UsuarioWebController controller;

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

    private UsuarioResponse usuarioResponse(UUID id, String nome, Perfil perfil) {
        return new UsuarioResponse(id, nome, nome.toLowerCase().replace(" ", "") + "@email.com",
                perfil.name(), true);
    }

    private Authentication authMock(UUID usuarioId) {
        Authentication auth = mock(Authentication.class);
        when(usuarioLogado.obterUsuarioId(auth)).thenReturn(usuarioId);
        return auth;
    }

    // -------------------------------------------------------------------------
    // GET /web/usuarios
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /web/usuarios")
    class Listar {

        /**
         * [USR-C01] Com standaloneSetup (sem contexto Spring Security), o controller responde
         * diretamente sem filtros de autorização. A restrição hasRole('ADMIN') é validada
         * pela cadeia web do SecurityConfig — coberta pelo FrontendIntegrationTest.
         */
        @Test
        @DisplayName("[USR-C01] GET /web/usuarios deve retornar view 'usuario/lista' com atributo 'page'")
        void lista_deveRetornarViewListaComPaginacao() throws Exception {
            // Arrange
            var response = usuarioResponse(randomId(), "Admin Teste", Perfil.ADMIN);
            Page<UsuarioResponse> page = new PageImpl<>(List.of(response));
            when(usuarioService.listar(any())).thenReturn(page);

            // Act + Assert
            mockMvc.perform(get("/web/usuarios"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("usuario/lista"))
                    .andExpect(model().attributeExists("page"));
        }

        /**
         * [USR-C02] A restrição de acesso para OPERADOR (403) é responsabilidade do
         * SecurityConfig e é validada no FrontendIntegrationTest com contexto completo.
         * Aqui documentamos a expectativa de comportamento sem segurança ativa.
         */
        @Test
        @DisplayName("[USR-C02] GET /web/usuarios delega ao service independente do perfil do caller")
        void lista_delegaAoServiceSemFiltroDeSegurancaNoUnitario() throws Exception {
            // Arrange
            Page<UsuarioResponse> page = new PageImpl<>(List.of());
            when(usuarioService.listar(any())).thenReturn(page);

            // Act + Assert — sem contexto de segurança, controller apenas delega ao service
            mockMvc.perform(get("/web/usuarios"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("usuario/lista"));

            verify(usuarioService).listar(any());
        }
    }

    // -------------------------------------------------------------------------
    // GET /web/usuarios/novo
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /web/usuarios/novo")
    class NovoForm {

        @Test
        @DisplayName("deve retornar view 'usuario/novo' com usuarioForm e lista de perfis")
        void novoForm_deveRetornarViewComFormEPerfis() throws Exception {
            mockMvc.perform(get("/web/usuarios/novo"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("usuario/novo"))
                    .andExpect(model().attributeExists("usuarioForm"))
                    .andExpect(model().attributeExists("perfis"));
        }
    }

    // -------------------------------------------------------------------------
    // POST /web/usuarios
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /web/usuarios")
    class Criar {

        @Test
        @DisplayName("[USR-C03] POST /web/usuarios com dados válidos deve redirecionar para /web/usuarios")
        void criar_comDadosValidos_deveRedirecionarComFlashSucesso() throws Exception {
            // Arrange
            var response = usuarioResponse(randomId(), "Novo Usuario", Perfil.OPERADOR);
            when(usuarioService.criar(any())).thenReturn(response);

            // Act + Assert
            mockMvc.perform(post("/web/usuarios")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("nome", "Novo Usuario")
                            .param("email", "novousuario@email.com")
                            .param("senha", "senha123")
                            .param("confirmarSenha", "senha123")
                            .param("perfil", "OPERADOR"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/usuarios"))
                    .andExpect(flash().attribute("sucesso", "Usuário criado com sucesso."));

            verify(usuarioService).criar(any());
        }

        @Test
        @DisplayName("[USR-C04] POST /web/usuarios com email duplicado deve re-renderizar form com erro")
        void criar_comEmailDuplicado_deveVoltarAoFormComErroDeEmail() throws Exception {
            // Arrange
            when(usuarioService.criar(any()))
                    .thenThrow(new EmailJaCadastradoException("duplicado@email.com"));

            // Act + Assert
            mockMvc.perform(post("/web/usuarios")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("nome", "Usuario Dup")
                            .param("email", "duplicado@email.com")
                            .param("senha", "senha123")
                            .param("confirmarSenha", "senha123")
                            .param("perfil", "OPERADOR"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("usuario/novo"))
                    .andExpect(model().attributeExists("perfis"))
                    .andExpect(model().attributeHasFieldErrors("usuarioForm", "email"));
        }

        @Test
        @DisplayName("POST /web/usuarios com senhas divergentes deve re-renderizar form com erro")
        void criar_comSenhasDivergentes_deveVoltarAoFormComErroDeSenha() throws Exception {
            // Act + Assert
            mockMvc.perform(post("/web/usuarios")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("nome", "Usuario X")
                            .param("email", "usuariox@email.com")
                            .param("senha", "senha123")
                            .param("confirmarSenha", "senhadiferente")
                            .param("perfil", "OPERADOR"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("usuario/novo"))
                    .andExpect(model().attributeHasFieldErrors("usuarioForm", "confirmarSenha"));

            verify(usuarioService, never()).criar(any());
        }

        @Test
        @DisplayName("POST /web/usuarios com dados inválidos (nome em branco) deve re-renderizar form com erro de bean validation")
        void criar_comNomeEmBranco_deveVoltarAoFormComErros() throws Exception {
            // Act + Assert
            mockMvc.perform(post("/web/usuarios")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("nome", "")
                            .param("email", "valido@email.com")
                            .param("senha", "senha123")
                            .param("confirmarSenha", "senha123")
                            .param("perfil", "OPERADOR"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("usuario/novo"))
                    .andExpect(model().attributeHasFieldErrors("usuarioForm", "nome"));

            verify(usuarioService, never()).criar(any());
        }
    }

    // -------------------------------------------------------------------------
    // GET /web/usuarios/{id}/editar
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /web/usuarios/{id}/editar")
    class EditarForm {

        @Test
        @DisplayName("deve retornar view 'usuario/editar' com form preenchido e lista de perfis")
        void editarForm_deveRetornarViewComDadosDoUsuario() throws Exception {
            // Arrange
            UUID id = randomId();
            var response = usuarioResponse(id, "Usuario Edit", Perfil.OPERADOR);
            when(usuarioService.buscarPorId(id)).thenReturn(response);

            // Act + Assert
            mockMvc.perform(get("/web/usuarios/{id}/editar", id))
                    .andExpect(status().isOk())
                    .andExpect(view().name("usuario/editar"))
                    .andExpect(model().attributeExists("usuarioForm"))
                    .andExpect(model().attributeExists("perfis"))
                    .andExpect(model().attribute("usuarioId", id));
        }
    }

    // -------------------------------------------------------------------------
    // POST /web/usuarios/{id}
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /web/usuarios/{id}")
    class Editar {

        @Test
        @DisplayName("editar com dados válidos deve redirecionar para /web/usuarios com flash sucesso")
        void editar_comDadosValidos_deveRedirecionarComFlashSucesso() throws Exception {
            // Arrange
            UUID id = randomId();
            UUID idAdmin = randomId();
            Authentication auth = authMock(idAdmin);
            var response = usuarioResponse(id, "Usuario Editado", Perfil.OPERADOR);
            when(usuarioService.editar(eq(id), any(), eq(idAdmin))).thenReturn(response);

            // Act + Assert — novaSenha omitido: Spring injeta null, @Size(min=8) não é aplicado
            mockMvc.perform(post("/web/usuarios/{id}", id)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("nome", "Usuario Editado")
                            .param("email", "editado@email.com")
                            .param("perfil", "OPERADOR")
                            .principal(auth))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/usuarios"))
                    .andExpect(flash().attribute("sucesso", "Usuário atualizado com sucesso."));

            verify(usuarioService).editar(eq(id), any(), eq(idAdmin));
        }

        @Test
        @DisplayName("editar com nova senha divergente deve re-renderizar form com erro de senha")
        void editar_comNovaSenhaDivergente_deveVoltarAoFormComErro() throws Exception {
            // Arrange
            UUID id = randomId();

            // Act + Assert
            mockMvc.perform(post("/web/usuarios/{id}", id)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("nome", "Usuario X")
                            .param("email", "usuariox@email.com")
                            .param("perfil", "OPERADOR")
                            .param("novaSenha", "novasenha123")
                            .param("confirmarNovaSenha", "senhadiferente"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("usuario/editar"))
                    .andExpect(model().attributeHasFieldErrors("usuarioForm", "confirmarNovaSenha"));

            verify(usuarioService, never()).editar(any(), any(), any());
        }

        @Test
        @DisplayName("editar com email duplicado deve re-renderizar form com erro de email")
        void editar_comEmailDuplicado_deveVoltarAoFormComErroDeEmail() throws Exception {
            // Arrange
            UUID id = randomId();
            UUID idAdmin = randomId();
            Authentication auth = authMock(idAdmin);
            when(usuarioService.editar(eq(id), any(), eq(idAdmin)))
                    .thenThrow(new EmailJaCadastradoException("dup@email.com"));

            // Act + Assert — novaSenha omitido para evitar falha em @Size(min=8) com string vazia
            mockMvc.perform(post("/web/usuarios/{id}", id)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("nome", "Usuario Dup")
                            .param("email", "dup@email.com")
                            .param("perfil", "OPERADOR")
                            .principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(view().name("usuario/editar"))
                    .andExpect(model().attributeHasFieldErrors("usuarioForm", "email"));
        }

        @Test
        @DisplayName("editar com OperacaoNegadaException deve redirecionar com flash erro")
        void editar_comOperacaoNegada_deveRedirecionarComFlashErro() throws Exception {
            // Arrange
            UUID id = randomId();
            UUID idAdmin = randomId();
            Authentication auth = authMock(idAdmin);
            when(usuarioService.editar(eq(id), any(), eq(idAdmin)))
                    .thenThrow(new OperacaoNegadaException("Você não pode alterar o próprio perfil."));

            // Act + Assert — novaSenha omitido para evitar falha em @Size(min=8) com string vazia
            mockMvc.perform(post("/web/usuarios/{id}", id)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("nome", "Admin")
                            .param("email", "admin@email.com")
                            .param("perfil", "OPERADOR")
                            .principal(auth))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/usuarios"))
                    .andExpect(flash().attribute("erro", "Você não pode alterar o próprio perfil."));
        }
    }

    // -------------------------------------------------------------------------
    // POST /web/usuarios/{id}/desativar
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /web/usuarios/{id}/desativar")
    class Desativar {

        @Test
        @DisplayName("desativar com sucesso deve redirecionar com flash sucesso")
        void desativar_comSucesso_deveRedirecionarComFlashSucesso() throws Exception {
            // Arrange
            UUID id = randomId();
            UUID idAdmin = randomId();
            Authentication auth = authMock(idAdmin);
            doNothing().when(usuarioService).desativar(eq(id), eq(idAdmin));

            // Act + Assert
            mockMvc.perform(post("/web/usuarios/{id}/desativar", id)
                            .principal(auth))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/usuarios"))
                    .andExpect(flash().attribute("sucesso", "Usuário desativado."));

            verify(usuarioService).desativar(eq(id), eq(idAdmin));
        }

        @Test
        @DisplayName("[USR-C06] POST /web/usuarios/{id}/desativar conta própria deve redirecionar com flash erro")
        void desativar_contaPropria_deveRedirecionarComFlashErro() throws Exception {
            // Arrange
            UUID idAdmin = randomId();
            Authentication auth = authMock(idAdmin);
            doThrow(new OperacaoNegadaException("Você não pode desativar a própria conta."))
                    .when(usuarioService).desativar(eq(idAdmin), eq(idAdmin));

            // Act + Assert
            mockMvc.perform(post("/web/usuarios/{id}/desativar", idAdmin)
                            .principal(auth))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/usuarios"))
                    .andExpect(flash().attribute("erro", "Você não pode desativar a própria conta."));
        }
    }

    // -------------------------------------------------------------------------
    // POST /web/usuarios/{id}/reativar
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /web/usuarios/{id}/reativar")
    class Reativar {

        @Test
        @DisplayName("reativar deve redirecionar com flash sucesso")
        void reativar_deveRedirecionarComFlashSucesso() throws Exception {
            // Arrange
            UUID id = randomId();
            doNothing().when(usuarioService).reativar(eq(id));

            // Act + Assert
            mockMvc.perform(post("/web/usuarios/{id}/reativar", id))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/usuarios"))
                    .andExpect(flash().attribute("sucesso", "Usuário reativado."));

            verify(usuarioService).reativar(eq(id));
        }
    }

    // -------------------------------------------------------------------------
    // GET /web/usuarios/registro
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /web/usuarios/registro")
    class RegistroForm {

        /**
         * [USR-C05] GET /web/usuarios/registro é público (permitAll no SecurityConfig).
         * Com standaloneSetup retorna 200 naturalmente — confirmando que o controller
         * não impõe nenhuma restrição própria no método.
         */
        @Test
        @DisplayName("[USR-C05] GET /web/usuarios/registro sem autenticação deve retornar 200 com view 'usuario/registro'")
        void registroForm_deveRetornarViewRegistroSemAutenticacao() throws Exception {
            mockMvc.perform(get("/web/usuarios/registro"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("usuario/registro"))
                    .andExpect(model().attributeExists("registroForm"));
        }
    }

    // -------------------------------------------------------------------------
    // POST /web/usuarios/registro
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /web/usuarios/registro")
    class Registro {

        @Test
        @DisplayName("registrar com dados válidos deve redirecionar para /web/login com flash sucesso")
        void registrar_comDadosValidos_deveRedirecionarParaLoginComFlashSucesso() throws Exception {
            // Arrange
            var response = usuarioResponse(randomId(), "Maria Nova", Perfil.VISUALIZADOR);
            when(usuarioService.registrar(any())).thenReturn(response);

            // Act + Assert
            mockMvc.perform(post("/web/usuarios/registro")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("nome", "Maria Nova")
                            .param("email", "marianava@email.com")
                            .param("senha", "senha123")
                            .param("confirmarSenha", "senha123"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/web/login"))
                    .andExpect(flash().attributeExists("sucesso"));

            verify(usuarioService).registrar(any());
        }

        @Test
        @DisplayName("registrar com email duplicado deve re-renderizar form com erro de email")
        void registrar_comEmailDuplicado_deveVoltarAoFormComErroDeEmail() throws Exception {
            // Arrange
            when(usuarioService.registrar(any()))
                    .thenThrow(new EmailJaCadastradoException("duplicado@email.com"));

            // Act + Assert
            mockMvc.perform(post("/web/usuarios/registro")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("nome", "Usuario Dup")
                            .param("email", "duplicado@email.com")
                            .param("senha", "senha123")
                            .param("confirmarSenha", "senha123"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("usuario/registro"))
                    .andExpect(model().attributeHasFieldErrors("registroForm", "email"));
        }

        @Test
        @DisplayName("registrar com senhas divergentes deve re-renderizar form com erro de confirmação")
        void registrar_comSenhasDivergentes_deveVoltarAoFormComErroDeConfirmacao() throws Exception {
            // Act + Assert
            mockMvc.perform(post("/web/usuarios/registro")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("nome", "Usuario Y")
                            .param("email", "usuarioy@email.com")
                            .param("senha", "senha123")
                            .param("confirmarSenha", "senhaerrada"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("usuario/registro"))
                    .andExpect(model().attributeHasFieldErrors("registroForm", "confirmarSenha"));

            verify(usuarioService, never()).registrar(any());
        }
    }
}
