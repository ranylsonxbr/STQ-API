package com.example.Stq.autenticacao.application.services;

import com.example.Stq.autenticacao.application.dto.CriarUsuarioRequest;
import com.example.Stq.autenticacao.application.dto.EditarUsuarioRequest;
import com.example.Stq.autenticacao.application.dto.RegistrarUsuarioRequest;
import com.example.Stq.autenticacao.application.dto.UsuarioResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UsuarioService {
    UsuarioResponse registrar(RegistrarUsuarioRequest request);
    UsuarioResponse criar(CriarUsuarioRequest request);
    UsuarioResponse editar(UUID id, EditarUsuarioRequest request, UUID idAdmin);
    void desativar(UUID id, UUID idAdmin);
    void reativar(UUID id);
    Page<UsuarioResponse> listar(Pageable pageable);
    UsuarioResponse buscarPorId(UUID id);
}
