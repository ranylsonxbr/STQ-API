package com.example.Stq.autenticacao.application;

import com.example.Stq.autenticacao.application.dto.LoginRequest;
import com.example.Stq.autenticacao.application.dto.LoginResponse;
import com.example.Stq.autenticacao.application.dto.RefreshRequest;
import com.example.Stq.autenticacao.domain.Perfil;
import com.example.Stq.autenticacao.domain.Usuario;
import com.example.Stq.autenticacao.domain.UsuarioRepository;
import com.example.Stq.autenticacao.domain.exception.ContaDesativadaException;
import com.example.Stq.autenticacao.domain.exception.CredenciaisInvalidasException;
import com.example.Stq.config.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AutenticacaoServiceImpl service;

    private Usuario usuarioAtivo;

    @BeforeEach
    void setUp() {
        usuarioAtivo = Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Teste")
                .email("teste@email.com")
                .senha("hash")
                .perfil(Perfil.OPERADOR)
                .ativo(true)
                .build();
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("[AUTH-C1] deve retornar tokens quando credenciais são válidas")
        void deveRetornarTokensQuandoCredenciaisValidas() {
            when(usuarioRepository.findByEmail("teste@email.com")).thenReturn(Optional.of(usuarioAtivo));
            when(passwordEncoder.matches("senha123", "hash")).thenReturn(true);
            when(jwtService.gerarAccessToken(usuarioAtivo)).thenReturn("access-token");
            when(jwtService.gerarRefreshToken(usuarioAtivo)).thenReturn("refresh-token");
            when(jwtService.getAccessTokenExpiration()).thenReturn(28800L);

            LoginResponse response = service.login(new LoginRequest("teste@email.com", "senha123"));

            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");
            assertThat(response.expiresIn()).isEqualTo(28800L);
        }

        @Test
        @DisplayName("[AUTH-C2] deve lançar CredenciaisInvalidasException quando email não existe")
        void deveLancarExcecaoQuandoEmailNaoExiste() {
            when(usuarioRepository.findByEmail("naoexiste@email.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.login(new LoginRequest("naoexiste@email.com", "qualquer")))
                    .isInstanceOf(CredenciaisInvalidasException.class);
        }

        @Test
        @DisplayName("[AUTH-C2] deve lançar CredenciaisInvalidasException quando senha está errada")
        void deveLancarExcecaoQuandoSenhaErrada() {
            when(usuarioRepository.findByEmail("teste@email.com")).thenReturn(Optional.of(usuarioAtivo));
            when(passwordEncoder.matches("errada", "hash")).thenReturn(false);

            assertThatThrownBy(() -> service.login(new LoginRequest("teste@email.com", "errada")))
                    .isInstanceOf(CredenciaisInvalidasException.class);
        }

        @Test
        @DisplayName("[AUTH-C3] deve lançar ContaDesativadaException quando usuário está inativo")
        void deveLancarExcecaoQuandoUsuarioInativo() {
            usuarioAtivo.setAtivo(false);
            when(usuarioRepository.findByEmail("teste@email.com")).thenReturn(Optional.of(usuarioAtivo));

            assertThatThrownBy(() -> service.login(new LoginRequest("teste@email.com", "qualquer")))
                    .isInstanceOf(ContaDesativadaException.class);
        }
    }

    @Nested
    @DisplayName("refresh")
    class Refresh {

        @Test
        @DisplayName("[AUTH-C4] deve retornar novo accessToken com refreshToken válido")
        void deveRetornarNovoAccessTokenComRefreshTokenValido() {
            when(jwtService.extrairEmailDoRefreshToken("refresh-token")).thenReturn("teste@email.com");
            when(usuarioRepository.findByEmail("teste@email.com")).thenReturn(Optional.of(usuarioAtivo));
            when(jwtService.gerarAccessToken(usuarioAtivo)).thenReturn("novo-access-token");
            when(jwtService.getAccessTokenExpiration()).thenReturn(28800L);

            LoginResponse response = service.refresh(new RefreshRequest("refresh-token"));

            assertThat(response.accessToken()).isEqualTo("novo-access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");
        }
    }
}
