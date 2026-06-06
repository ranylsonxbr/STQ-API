package com.example.Stq.produto.application.services;

import com.example.Stq.produto.application.dto.ProdutoCreateRequest;
import com.example.Stq.produto.application.dto.ProdutoDetalheResponse;
import com.example.Stq.produto.application.dto.ProdutoResponse;
import com.example.Stq.produto.application.dto.ProdutoUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProdutoService {
    ProdutoResponse criar(ProdutoCreateRequest request);
    ProdutoResponse atualizar(UUID id, ProdutoUpdateRequest request);
    void desativar(UUID id);
    ProdutoDetalheResponse buscarPorId(UUID id);
    ProdutoDetalheResponse buscarPorSku(String sku);
    Page<ProdutoResponse> listar(UUID categoriaId, Boolean ativo, String nome, Pageable pageable);
}
