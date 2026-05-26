package com.example.Stq.produto.domain.exception;

public class SkuColisaoException extends RuntimeException {
    public SkuColisaoException() {
        super("Falha ao gerar SKU único após 5 tentativas. Tente novamente.");
    }
}
