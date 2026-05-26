package com.example.Stq.autenticacao.infra;

import com.example.Stq.autenticacao.domain.Usuario;
import com.example.Stq.autenticacao.domain.UsuarioRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UsuarioJpaRepository extends JpaRepository<Usuario, UUID>, UsuarioRepository {
}
