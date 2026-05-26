package com.example.Stq.produto.infra.adapter;

import com.example.Stq.produto.domain.port.PedidoCompraReadPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Stub do port PedidoCompraReadPort.
 * Retorna sempre false enquanto o módulo pedido-compra não for implementado.
 * DEBT: substituir quando PedidoCompra for implementado (ver sdd/backlog.md).
 */
@Component
public class PedidoCompraReadAdapter implements PedidoCompraReadPort {

    @Override
    public boolean existePedidoEmAbertoParaProduto(UUID produtoId) {
        return false;
    }

    @Override
    public boolean existePedidoEmAbertoParaCategoria(UUID categoriaId) {
        return false;
    }
}
