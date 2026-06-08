package com.example.Stq.pedidocompra.infra;

import com.example.Stq.pedidocompra.domain.PedidoCompra;
import com.example.Stq.pedidocompra.domain.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PedidoCompraJpaRepository
        extends JpaRepository<PedidoCompra, UUID>, JpaSpecificationExecutor<PedidoCompra> {

    @Query("""
            select (count(i) > 0) from ItemPedidoCompra i
            where i.produto.id = :produtoId and i.pedidoCompra.status in :status
            """)
    boolean existeItemDeProdutoComStatus(@Param("produtoId") UUID produtoId,
                                         @Param("status") List<StatusPedido> status);

    @Query("""
            select (count(i) > 0) from ItemPedidoCompra i
            where i.produto.categoria.id = :categoriaId and i.pedidoCompra.status in :status
            """)
    boolean existeItemDeCategoriaComStatus(@Param("categoriaId") UUID categoriaId,
                                           @Param("status") List<StatusPedido> status);

    @Query("""
            select (count(p) > 0) from PedidoCompra p
            where p.fornecedor.id = :fornecedorId and p.status in :status
            """)
    boolean existePedidoDeFornecedorComStatus(@Param("fornecedorId") UUID fornecedorId,
                                              @Param("status") List<StatusPedido> status);
}
