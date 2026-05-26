package com.example.Stq.produto.domain.exception;

import java.util.UUID;

public class ProdutoNotFoundException extends RuntimeException {
    public ProdutoNotFoundException(UUID id) {
        super("Produto não encontrado: " + id);
    }

    public ProdutoNotFoundException(String sku) {
        super("Produto não encontrado com SKU: " + sku);
    }
}
