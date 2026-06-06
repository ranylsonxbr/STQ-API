package com.example.Stq.fornecedor.domain.exception;

public class FornecedorComPedidoEmAbertoException extends RuntimeException {
    public FornecedorComPedidoEmAbertoException() {
        super("Fornecedor possui pedido de compra em aberto.");
    }
}
