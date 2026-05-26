package com.example.Stq.produto.domain.exception;

public class ProdutoComPedidoEmAbertoException extends RuntimeException {
    public ProdutoComPedidoEmAbertoException() {
        super("Produto possui pedido em PENDENTE ou APROVADO e não pode ser desativado (RN-07).");
    }
}
