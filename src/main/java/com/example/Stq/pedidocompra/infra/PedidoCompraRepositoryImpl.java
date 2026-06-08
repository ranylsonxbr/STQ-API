package com.example.Stq.pedidocompra.infra;

import com.example.Stq.pedidocompra.domain.PedidoCompra;
import com.example.Stq.pedidocompra.domain.PedidoCompraFiltro;
import com.example.Stq.pedidocompra.domain.PedidoCompraRepository;
import com.example.Stq.pedidocompra.domain.StatusPedido;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PedidoCompraRepositoryImpl implements PedidoCompraRepository {

    private static final List<StatusPedido> STATUS_ABERTOS =
            List.of(StatusPedido.PENDENTE, StatusPedido.APROVADO);

    private final PedidoCompraJpaRepository jpa;

    @Override
    public PedidoCompra save(PedidoCompra pedido) {
        return jpa.save(pedido);
    }

    @Override
    public Optional<PedidoCompra> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Page<PedidoCompra> findAll(PedidoCompraFiltro filtro, Pageable pageable) {
        return jpa.findAll(PedidoCompraSpecs.comFiltro(filtro), pageable);
    }

    @Override
    public boolean existePedidoAbertoParaProduto(UUID produtoId) {
        return jpa.existeItemDeProdutoComStatus(produtoId, STATUS_ABERTOS);
    }

    @Override
    public boolean existePedidoAbertoParaCategoria(UUID categoriaId) {
        return jpa.existeItemDeCategoriaComStatus(categoriaId, STATUS_ABERTOS);
    }

    @Override
    public boolean existePedidoAbertoParaFornecedor(UUID fornecedorId) {
        return jpa.existePedidoDeFornecedorComStatus(fornecedorId, STATUS_ABERTOS);
    }
}
