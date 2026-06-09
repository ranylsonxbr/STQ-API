package com.example.Stq.autenticacao.application.services;

import com.example.Stq.autenticacao.application.dto.CriarUsuarioRequest;
import com.example.Stq.autenticacao.application.dto.EditarUsuarioRequest;
import com.example.Stq.autenticacao.application.dto.RegistrarUsuarioRequest;
import com.example.Stq.autenticacao.application.dto.UsuarioResponse;
import com.example.Stq.autenticacao.domain.Perfil;
import com.example.Stq.autenticacao.domain.Usuario;
import com.example.Stq.autenticacao.domain.UsuarioRepository;
import com.example.Stq.autenticacao.domain.exception.EmailJaCadastradoException;
import com.example.Stq.autenticacao.domain.exception.OperacaoNegadaException;
import com.example.Stq.autenticacao.domain.exception.UsuarioNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioServiceImpl")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl service;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Usuario usuarioComId(UUID id, String nome, String email, Perfil perfil) {
        return Usuario.builder()
                .id(id)
                .nome(nome)
                .email(email)
                .senha("hash-senha")
                .perfil(perfil)
                .ativo(true)
                .build();
    }

    // -------------------------------------------------------------------------
    // registrar
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("registrar")
    class Registrar {

        @Test
        @DisplayName("[USR-S01] registrar com email novo deve criar usuário com perfil VISUALIZADOR")
        void registrar_comEmailNovo_deveCriarUsuarioComPerfilVisualizador() {
            // Arrange
            var request = new RegistrarUsuarioRequest("Maria Silva", "maria@email.com", "senha123");
            var usuarioSalvo = usuarioComId(UUID.randomUUID(), "Maria Silva", "maria@email.com", Perfil.VISUALIZADOR);

            when(usuarioRepository.existsByEmail("maria@email.com")).thenReturn(false);
            when(passwordEncoder.encode("senha123")).thenReturn("hash-senha");
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioSalvo);

            // Act
            UsuarioResponse response = service.registrar(request);

            // Assert
            assertThat(response.nome()).isEqualTo("Maria Silva");
            assertThat(response.email()).isEqualTo("maria@email.com");
            assertThat(response.perfil()).isEqualTo(Perfil.VISUALIZADOR.name());
            assertThat(response.ativo()).isTrue();

            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepository).save(captor.capture());
            assertThat(captor.getValue().getPerfil()).isEqualTo(Perfil.VISUALIZADOR);
            assertThat(captor.getValue().getSenha()).isEqualTo("hash-senha");
        }

        @Test
        @DisplayName("[USR-S02] registrar com email duplicado deve lançar EmailJaCadastradoException")
        void registrar_comEmailDuplicado_deveLancarEmailJaCadastradoException() {
            // Arrange
            var request = new RegistrarUsuarioRequest("João", "existente@email.com", "senha123");
            when(usuarioRepository.existsByEmail("existente@email.com")).thenReturn(true);

            // Act + Assert
            assertThatThrownBy(() -> service.registrar(request))
                    .isInstanceOf(EmailJaCadastradoException.class)
                    .hasMessageContaining("existente@email.com");

            verify(usuarioRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // criar
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("criar")
    class Criar {

        @Test
        @DisplayName("[USR-S03] criar com perfil OPERADOR deve persistir com perfil correto")
        void criar_comPerfilOperador_devePersistirComPerfilCorreto() {
            // Arrange
            var request = new CriarUsuarioRequest("Carlos", "carlos@email.com", "senha123", Perfil.OPERADOR);
            var usuarioSalvo = usuarioComId(UUID.randomUUID(), "Carlos", "carlos@email.com", Perfil.OPERADOR);

            when(usuarioRepository.existsByEmail("carlos@email.com")).thenReturn(false);
            when(passwordEncoder.encode("senha123")).thenReturn("hash-senha");
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioSalvo);

            // Act
            UsuarioResponse response = service.criar(request);

            // Assert
            assertThat(response.perfil()).isEqualTo(Perfil.OPERADOR.name());

            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepository).save(captor.capture());
            assertThat(captor.getValue().getPerfil()).isEqualTo(Perfil.OPERADOR);
        }

        @Test
        @DisplayName("criar com email duplicado deve lançar EmailJaCadastradoException")
        void criar_comEmailDuplicado_deveLancarEmailJaCadastradoException() {
            // Arrange
            var request = new CriarUsuarioRequest("Ana", "ana@email.com", "senha123", Perfil.OPERADOR);
            when(usuarioRepository.existsByEmail("ana@email.com")).thenReturn(true);

            // Act + Assert
            assertThatThrownBy(() -> service.criar(request))
                    .isInstanceOf(EmailJaCadastradoException.class)
                    .hasMessageContaining("ana@email.com");

            verify(usuarioRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // desativar
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("desativar")
    class Desativar {

        @Test
        @DisplayName("[USR-S04] desativar deve setar ativo=false")
        void desativar_deveSetarAtivoFalse() {
            // Arrange
            UUID idAdmin = UUID.randomUUID();
            UUID idAlvo = UUID.randomUUID();
            var usuario = usuarioComId(idAlvo, "Pedro", "pedro@email.com", Perfil.OPERADOR);

            when(usuarioRepository.findById(idAlvo)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

            // Act
            service.desativar(idAlvo, idAdmin);

            // Assert
            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepository).save(captor.capture());
            assertThat(captor.getValue().isAtivo()).isFalse();
        }

        @Test
        @DisplayName("[USR-S05] desativar conta própria deve lançar OperacaoNegadaException")
        void desativar_contaPropria_deveLancarOperacaoNegadaException() {
            // Arrange
            UUID idAdmin = UUID.randomUUID();

            // Act + Assert
            assertThatThrownBy(() -> service.desativar(idAdmin, idAdmin))
                    .isInstanceOf(OperacaoNegadaException.class)
                    .hasMessageContaining("própria conta");

            verify(usuarioRepository, never()).findById(any());
            verify(usuarioRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // editar
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("editar")
    class Editar {

        @Test
        @DisplayName("[USR-S06] editar sem nova senha deve manter senha original")
        void editar_semNovaSenha_deveManterSenhaOriginal() {
            // Arrange
            UUID idAdmin = UUID.randomUUID();
            UUID idAlvo = UUID.randomUUID();
            var usuario = usuarioComId(idAlvo, "Lucas", "lucas@email.com", Perfil.OPERADOR);
            var request = new EditarUsuarioRequest("Lucas Novo", "lucas@email.com", Perfil.OPERADOR, null);

            when(usuarioRepository.findById(idAlvo)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.existsByEmailAndIdNot("lucas@email.com", idAlvo)).thenReturn(false);
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

            // Act
            service.editar(idAlvo, request, idAdmin);

            // Assert
            verify(passwordEncoder, never()).encode(anyString());

            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepository).save(captor.capture());
            assertThat(captor.getValue().getSenha()).isEqualTo("hash-senha");
        }

        @Test
        @DisplayName("[USR-S07] editar com nova senha deve re-encodar")
        void editar_comNovaSenha_deveReencodarSenha() {
            // Arrange
            UUID idAdmin = UUID.randomUUID();
            UUID idAlvo = UUID.randomUUID();
            var usuario = usuarioComId(idAlvo, "Paula", "paula@email.com", Perfil.OPERADOR);
            var request = new EditarUsuarioRequest("Paula", "paula@email.com", Perfil.OPERADOR, "novaSenha123");

            when(usuarioRepository.findById(idAlvo)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.existsByEmailAndIdNot("paula@email.com", idAlvo)).thenReturn(false);
            when(passwordEncoder.encode("novaSenha123")).thenReturn("novo-hash");
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

            // Act
            service.editar(idAlvo, request, idAdmin);

            // Assert
            verify(passwordEncoder).encode("novaSenha123");

            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepository).save(captor.capture());
            assertThat(captor.getValue().getSenha()).isEqualTo("novo-hash");
        }

        @Test
        @DisplayName("[USR-S08] ADMIN editar próprio perfil deve lançar OperacaoNegadaException")
        void editar_adminAlterandoProprioPerfilParaDiferente_deveLancarOperacaoNegadaException() {
            // Arrange
            UUID idAdmin = UUID.randomUUID();
            var admin = usuarioComId(idAdmin, "Admin", "admin@email.com", Perfil.ADMIN);
            // Tenta alterar o próprio perfil de ADMIN para OPERADOR
            var request = new EditarUsuarioRequest("Admin", "admin@email.com", Perfil.OPERADOR, null);

            when(usuarioRepository.findById(idAdmin)).thenReturn(Optional.of(admin));
            when(usuarioRepository.existsByEmailAndIdNot("admin@email.com", idAdmin)).thenReturn(false);

            // Act + Assert
            assertThatThrownBy(() -> service.editar(idAdmin, request, idAdmin))
                    .isInstanceOf(OperacaoNegadaException.class)
                    .hasMessageContaining("próprio perfil");

            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("editar com email duplicado em outro usuário deve lançar EmailJaCadastradoException")
        void editar_comEmailEmUsoEmOutroUsuario_deveLancarEmailJaCadastradoException() {
            // Arrange
            UUID idAdmin = UUID.randomUUID();
            UUID idAlvo = UUID.randomUUID();
            var usuario = usuarioComId(idAlvo, "Bruno", "bruno@email.com", Perfil.OPERADOR);
            var request = new EditarUsuarioRequest("Bruno", "duplicado@email.com", Perfil.OPERADOR, null);

            when(usuarioRepository.findById(idAlvo)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.existsByEmailAndIdNot("duplicado@email.com", idAlvo)).thenReturn(true);

            // Act + Assert
            assertThatThrownBy(() -> service.editar(idAlvo, request, idAdmin))
                    .isInstanceOf(EmailJaCadastradoException.class)
                    .hasMessageContaining("duplicado@email.com");

            verify(usuarioRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // reativar
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("reativar")
    class Reativar {

        @Test
        @DisplayName("[USR-S09] reativar deve setar ativo=true")
        void reativar_deveSetarAtivoTrue() {
            // Arrange
            UUID id = UUID.randomUUID();
            var usuario = usuarioComId(id, "Bia", "bia@email.com", Perfil.VISUALIZADOR);
            usuario.setAtivo(false);

            when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

            // Act
            service.reativar(id);

            // Assert
            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepository).save(captor.capture());
            assertThat(captor.getValue().isAtivo()).isTrue();
        }

        @Test
        @DisplayName("reativar usuário inexistente deve lançar UsuarioNaoEncontradoException")
        void reativar_usuarioInexistente_deveLancarUsuarioNaoEncontradoException() {
            // Arrange
            UUID id = UUID.randomUUID();
            when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> service.reativar(id))
                    .isInstanceOf(UsuarioNaoEncontradoException.class)
                    .hasMessageContaining(id.toString());
        }
    }

    // -------------------------------------------------------------------------
    // buscarPorId
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorId {

        @Test
        @DisplayName("buscarPorId quando existe deve retornar UsuarioResponse")
        void buscarPorId_quandoExiste_deveRetornarResponse() {
            // Arrange
            UUID id = UUID.randomUUID();
            var usuario = usuarioComId(id, "Rone", "rone@email.com", Perfil.ADMIN);
            when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

            // Act
            UsuarioResponse response = service.buscarPorId(id);

            // Assert
            assertThat(response.id()).isEqualTo(id);
            assertThat(response.nome()).isEqualTo("Rone");
            assertThat(response.perfil()).isEqualTo(Perfil.ADMIN.name());
        }

        @Test
        @DisplayName("buscarPorId quando não existe deve lançar UsuarioNaoEncontradoException")
        void buscarPorId_quandoNaoExiste_deveLancarUsuarioNaoEncontradoException() {
            // Arrange
            UUID id = UUID.randomUUID();
            when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> service.buscarPorId(id))
                    .isInstanceOf(UsuarioNaoEncontradoException.class)
                    .hasMessageContaining(id.toString());
        }
    }

    // -------------------------------------------------------------------------
    // listar
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("listar")
    class Listar {

        @Test
        @DisplayName("listar deve retornar página de UsuarioResponse mapeada do repositório")
        void listar_deveRetornarPaginaMapeada() {
            // Arrange
            var pageable = PageRequest.of(0, 20);
            var usuario = usuarioComId(UUID.randomUUID(), "Theo", "theo@email.com", Perfil.VISUALIZADOR);
            var page = new PageImpl<>(List.of(usuario), pageable, 1);
            when(usuarioRepository.findAll(pageable)).thenReturn(page);

            // Act
            var resultado = service.listar(pageable);

            // Assert
            assertThat(resultado.getTotalElements()).isEqualTo(1);
            assertThat(resultado.getContent().get(0).nome()).isEqualTo("Theo");
        }
    }
}
