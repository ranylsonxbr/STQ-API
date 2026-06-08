package com.example.Stq.produto.infra.adapter;

import com.example.Stq.pedidocompra.domain.PedidoCompraRepository;
import com.example.Stq.produto.domain.port.PedidoCompraReadPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PedidoCompraReadAdapter implements PedidoCompraReadPort {

    private final PedidoCompraRepository pedidoCompraRepository;

    @Override
    public boolean existePedidoEmAbertoParaProduto(UUID produtoId) {
        return pedidoCompraRepository.existePedidoAbertoParaProduto(produtoId);
    }

    @Override
    public boolean existePedidoEmAbertoParaCategoria(UUID categoriaId) {
        return pedidoCompraRepository.existePedidoAbertoParaCategoria(categoriaId);
    }
}
