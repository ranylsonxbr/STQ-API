package com.example.Stq.pedidocompra.domain.exception;

public class FornecedorInativoException extends RuntimeException {
    public FornecedorInativoException() {
        super("Não é possível criar pedido para um fornecedor inativo.");
    }
}
