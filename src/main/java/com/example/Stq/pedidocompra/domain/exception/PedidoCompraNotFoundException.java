package com.example.Stq.pedidocompra.domain.exception;

import java.util.UUID;

public class PedidoCompraNotFoundException extends RuntimeException {
    public PedidoCompraNotFoundException(UUID id) {
        super("Pedido de compra não encontrado: " + id);
    }
}
