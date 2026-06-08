package com.example.Stq.pedidocompra.domain;

import com.example.Stq.produto.domain.Produto;
import com.example.Stq.produto.domain.VariacaoProduto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
    name = "item_pedido_compra",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_item_pedido_produto_variacao",
        columnNames = {"pedido_compra_id", "produto_id", "variacao_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemPedidoCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_compra_id", nullable = false)
    private PedidoCompra pedidoCompra;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variacao_id")
    private VariacaoProduto variacao;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal precoUnitario;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    public void recalcularSubtotal() {
        this.subtotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    public boolean mesmaReferencia(UUID produtoId, UUID variacaoId) {
        UUID esteVariacaoId = variacao != null ? variacao.getId() : null;
        return produto.getId().equals(produtoId)
                && java.util.Objects.equals(esteVariacaoId, variacaoId);
    }
}
