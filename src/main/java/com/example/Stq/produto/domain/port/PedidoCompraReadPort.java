package com.example.Stq.produto.domain.port;

import java.util.UUID;

public interface PedidoCompraReadPort {
    boolean existePedidoEmAbertoParaProduto(UUID produtoId);
    boolean existePedidoEmAbertoParaCategoria(UUID categoriaId);
}
