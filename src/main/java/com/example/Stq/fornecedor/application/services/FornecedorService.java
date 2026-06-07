package com.example.Stq.fornecedor.application.services;

import com.example.Stq.autenticacao.domain.Perfil;
import com.example.Stq.fornecedor.application.dto.FornecedorRequest;
import com.example.Stq.fornecedor.application.dto.FornecedorResponse;
import com.example.Stq.fornecedor.domain.FornecedorFiltro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface FornecedorService {
    FornecedorResponse criar(FornecedorRequest request, UUID usuarioId);
    Page<FornecedorResponse> listar(FornecedorFiltro filtro, Pageable pageable);
    FornecedorResponse buscarPorId(UUID id);
    FornecedorResponse atualizar(UUID id, FornecedorRequest request, UUID usuarioId, Perfil perfil);
    void desativar(UUID id, UUID usuarioId);
    FornecedorResponse reativar(UUID id, UUID usuarioId);
}
