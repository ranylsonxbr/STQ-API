package com.example.Stq.pedidocompra.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface PedidoCompraRepository {
    PedidoCompra save(PedidoCompra pedido);
    Optional<PedidoCompra> findById(UUID id);
    Page<PedidoCompra> findAll(PedidoCompraFiltro filtro, Pageable pageable);
    boolean existePedidoAbertoParaProduto(UUID produtoId);
    boolean existePedidoAbertoParaCategoria(UUID categoriaId);
    boolean existePedidoAbertoParaFornecedor(UUID fornecedorId);
}
