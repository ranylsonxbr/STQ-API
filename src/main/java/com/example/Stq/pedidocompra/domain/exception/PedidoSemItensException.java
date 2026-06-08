package com.example.Stq.pedidocompra.domain.exception;

public class PedidoSemItensException extends RuntimeException {
    public PedidoSemItensException() {
        super("O pedido precisa de ao menos um item para ser enviado.");
    }
}
