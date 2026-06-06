package com.example.Stq.produto.application.services;

import com.example.Stq.produto.application.dto.VariacaoCreateRequest;
import com.example.Stq.produto.application.dto.VariacaoResponse;

import java.util.UUID;

public interface VariacaoProdutoService {
    VariacaoResponse adicionar(UUID produtoId, VariacaoCreateRequest request);
    void desativar(UUID produtoId, UUID variacaoId);
}
