package com.example.Stq.frontend.support;

import com.example.Stq.autenticacao.domain.Perfil;
import com.example.Stq.autenticacao.infra.UsuarioDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UsuarioLogado {

    public UUID obterUsuarioId(Authentication auth) {
        return extrairDetails(auth).getUsuario().getId();
    }

    public Perfil obterPerfil(Authentication auth) {
        return extrairDetails(auth).getUsuario().getPerfil();
    }

    public String obterNome(Authentication auth) {
        return extrairDetails(auth).getUsuario().getNome();
    }

    private UsuarioDetails extrairDetails(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof UsuarioDetails details)) {
            throw new IllegalStateException(
                    "Principal da sessão não é do tipo esperado. Usuário não está autenticado corretamente.");
        }
        return details;
    }
}
