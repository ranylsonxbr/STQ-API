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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UsuarioResponse registrar(RegistrarUsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new EmailJaCadastradoException(request.email());
        }
        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .perfil(Perfil.VISUALIZADOR)
                .build();
        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioResponse criar(CriarUsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new EmailJaCadastradoException(request.email());
        }
        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .perfil(request.perfil())
                .build();
        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioResponse editar(UUID id, EditarUsuarioRequest request, UUID idAdmin) {
        Usuario usuario = buscar(id);
        if (usuarioRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new EmailJaCadastradoException(request.email());
        }
        if (id.equals(idAdmin) && !request.perfil().equals(usuario.getPerfil())) {
            throw new OperacaoNegadaException("Você não pode alterar o próprio perfil.");
        }
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setPerfil(request.perfil());
        if (request.novaSenha() != null && !request.novaSenha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(request.novaSenha()));
        }
        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public void desativar(UUID id, UUID idAdmin) {
        if (id.equals(idAdmin)) {
            throw new OperacaoNegadaException("Você não pode desativar a própria conta.");
        }
        Usuario usuario = buscar(id);
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void reativar(UUID id) {
        Usuario usuario = buscar(id);
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }

    @Override
    public Page<UsuarioResponse> listar(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(UsuarioResponse::de);
    }

    @Override
    public UsuarioResponse buscarPorId(UUID id) {
        return UsuarioResponse.de(buscar(id));
    }

    private Usuario buscar(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));
    }
}
