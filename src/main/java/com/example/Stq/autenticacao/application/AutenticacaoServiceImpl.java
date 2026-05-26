package com.example.Stq.autenticacao.application;

import com.example.Stq.autenticacao.application.dto.LoginRequest;
import com.example.Stq.autenticacao.application.dto.LoginResponse;
import com.example.Stq.autenticacao.application.dto.RefreshRequest;
import com.example.Stq.autenticacao.domain.Usuario;
import com.example.Stq.autenticacao.domain.UsuarioRepository;
import com.example.Stq.autenticacao.domain.exception.ContaDesativadaException;
import com.example.Stq.autenticacao.domain.exception.CredenciaisInvalidasException;
import com.example.Stq.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AutenticacaoServiceImpl implements AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!usuario.isAtivo()) {
            throw new ContaDesativadaException();
        }

        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new CredenciaisInvalidasException();
        }

        return new LoginResponse(
                jwtService.gerarAccessToken(usuario),
                jwtService.gerarRefreshToken(usuario),
                jwtService.getAccessTokenExpiration()
        );
    }

    @Override
    public LoginResponse refresh(RefreshRequest request) {
        String email = jwtService.extrairEmailDoRefreshToken(request.refreshToken());

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!usuario.isAtivo()) {
            throw new ContaDesativadaException();
        }

        return new LoginResponse(
                jwtService.gerarAccessToken(usuario),
                request.refreshToken(),
                jwtService.getAccessTokenExpiration()
        );
    }
}
