package com.example.Stq.produto.infra.adapter;

import com.example.Stq.produto.domain.port.PedidoCompraReadPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

// Stub — retorna false até módulo pedido-compra ser implementado (backlog.md DD-05)
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
