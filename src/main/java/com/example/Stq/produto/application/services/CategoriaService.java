package com.example.Stq.produto.application.services;

import com.example.Stq.produto.application.dto.CategoriaCreateRequest;
import com.example.Stq.produto.application.dto.CategoriaResponse;
import com.example.Stq.produto.application.dto.CategoriaUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CategoriaService {
    CategoriaResponse criar(CategoriaCreateRequest request);
    CategoriaResponse atualizar(UUID id, CategoriaUpdateRequest request);
    void desativar(UUID id);
    CategoriaResponse buscarPorId(UUID id);
    Page<CategoriaResponse> listar(UUID categoriaPaiId, Boolean ativo, Pageable pageable);
}
