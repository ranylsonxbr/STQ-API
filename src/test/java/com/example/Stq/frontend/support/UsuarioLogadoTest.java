package com.example.Stq.frontend.support;

import com.example.Stq.autenticacao.domain.Perfil;
import com.example.Stq.autenticacao.domain.Usuario;
import com.example.Stq.autenticacao.infra.UsuarioDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("UsuarioLogado")
class UsuarioLogadoTest {

    private UsuarioLogado usuarioLogado;
    private UUID usuarioId;
    private Usuario usuario;
    private UsuarioDetails details;
    private Authentication auth;

    @BeforeEach
    void setUp() {
        usuarioLogado = new UsuarioLogado();
        usuarioId = UUID.randomUUID();
        usuario = Usuario.builder()
                .id(usuarioId)
                .nome("Ranylson Teste")
                .email("ranylson@teste.com")
                .senha("senha123")
                .perfil(Perfil.ADMIN)
                .build();
        details = new UsuarioDetails(usuario);
        auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(details);
    }

    @Nested
    @DisplayName("obterUsuarioId")
    class ObterUsuarioId {

        @Test
        @DisplayName("[FW-U1] deve retornar o UUID do usuario autenticado")
        void deveRetornarUuidDoUsuario() {
            // Act
            UUID resultado = usuarioLogado.obterUsuarioId(auth);

            // Assert
            assertThat(resultado).isEqualTo(usuarioId);
        }
    }

    @Nested
    @DisplayName("obterPerfil")
    class ObterPerfil {

        @Test
        @DisplayName("[FW-U2] deve retornar o perfil do usuario autenticado")
        void deveRetornarPerfilDoUsuario() {
            // Act
            Perfil perfil = usuarioLogado.obterPerfil(auth);

            // Assert
            assertThat(perfil).isEqualTo(Perfil.ADMIN);
        }

        @Test
        @DisplayName("[FW-U3] deve retornar OPERADOR quando usuario tem perfil OPERADOR")
        void deveRetornarPerfilOperador() {
            // Arrange
            var usuarioOp = Usuario.builder()
                    .id(UUID.randomUUID())
                    .nome("Operador")
                    .email("op@teste.com")
                    .senha("op123")
                    .perfil(Perfil.OPERADOR)
                    .build();
            var detailsOp = new UsuarioDetails(usuarioOp);
            var authOp = mock(Authentication.class);
            when(authOp.getPrincipal()).thenReturn(detailsOp);

            // Act
            Perfil perfil = usuarioLogado.obterPerfil(authOp);

            // Assert
            assertThat(perfil).isEqualTo(Perfil.OPERADOR);
        }
    }

    @Nested
    @DisplayName("obterNome")
    class ObterNome {

        @Test
        @DisplayName("[FW-U4] deve retornar o nome do usuario autenticado")
        void deveRetornarNomeDoUsuario() {
            // Act
            String nome = usuarioLogado.obterNome(auth);

            // Assert
            assertThat(nome).isEqualTo("Ranylson Teste");
        }
    }

    @Nested
    @DisplayName("principal incompativel")
    class PrincipalIncompativel {

        @Test
        @DisplayName("[FW-U5] deve lancar IllegalStateException quando principal nao e UsuarioDetails")
        void deveLancarExcecaoQuandoPrincipalIncompativel() {
            // Arrange
            Authentication authInvalido = mock(Authentication.class);
            when(authInvalido.getPrincipal()).thenReturn("nao-e-usuario-details");

            // Act + Assert
            assertThatThrownBy(() -> usuarioLogado.obterUsuarioId(authInvalido))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("não está autenticado corretamente");
        }

        @Test
        @DisplayName("[FW-U6] deve lancar IllegalStateException quando Authentication e null")
        void deveLancarExcecaoQuandoAuthNull() {
            // Act + Assert
            assertThatThrownBy(() -> usuarioLogado.obterUsuarioId(null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
