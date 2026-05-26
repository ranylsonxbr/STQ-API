package com.example.Stq.autenticacao.domain;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findById(UUID id);
    Usuario save(Usuario usuario);
}
