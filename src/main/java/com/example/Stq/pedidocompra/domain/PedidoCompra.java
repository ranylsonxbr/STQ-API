package com.example.Stq.pedidocompra.domain;

import com.example.Stq.autenticacao.domain.Usuario;
import com.example.Stq.fornecedor.domain.Fornecedor;
import com.example.Stq.pedidocompra.domain.exception.ItemDuplicadoException;
import com.example.Stq.pedidocompra.domain.exception.PedidoSemItensException;
import com.example.Stq.pedidocompra.domain.exception.TransicaoStatusInvalidaException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pedido_compra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fornecedor_id", nullable = false)
    private Fornecedor fornecedor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusPedido status = StatusPedido.RASCUNHO;

    @Column(name = "data_emissao", nullable = false)
    private LocalDate dataEmissao;

    @Column(name = "data_previsao_entrega")
    private LocalDate dataPrevisaoEntrega;

    @Column(length = 500)
    private String observacao;

    @Column(name = "total_pedido", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalPedido = BigDecimal.ZERO;

    @OneToMany(mappedBy = "pedidoCompra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @Builder.Default
    private List<ItemPedidoCompra> itens = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "criado_por", nullable = false)
    private Usuario criadoPor;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist
    void prePersist() {
        criadoEm = Instant.now();
    }

    public void recalcularTotal() {
        this.totalPedido = itens.stream()
                .map(ItemPedidoCompra::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void adicionarItem(ItemPedidoCompra item) {
        exigirRascunho("adicionar itens a");
        UUID variacaoId = item.getVariacao() != null ? item.getVariacao().getId() : null;
        boolean duplicado = itens.stream()
                .anyMatch(i -> i.mesmaReferencia(item.getProduto().getId(), variacaoId));
        if (duplicado) {
            throw new ItemDuplicadoException();
        }
        item.setPedidoCompra(this);
        item.recalcularSubtotal();
        itens.add(item);
        recalcularTotal();
    }

    public void removerItem(UUID itemId) {
        exigirRascunho("remover itens de");
        itens.removeIf(i -> i.getId().equals(itemId));
        recalcularTotal();
    }

    public void enviar() {
        if (status != StatusPedido.RASCUNHO) {
            throw new TransicaoStatusInvalidaException(status, "enviar");
        }
        if (itens.isEmpty()) {
            throw new PedidoSemItensException();
        }
        this.status = StatusPedido.PENDENTE;
    }

    public void aprovar() {
        if (status != StatusPedido.PENDENTE) {
            throw new TransicaoStatusInvalidaException(status, "aprovar");
        }
        this.status = StatusPedido.APROVADO;
    }

    public void receber() {
        if (status != StatusPedido.APROVADO) {
            throw new TransicaoStatusInvalidaException(status, "receber");
        }
        this.status = StatusPedido.RECEBIDO;
    }

    public void cancelar() {
        if (status != StatusPedido.RASCUNHO && status != StatusPedido.PENDENTE) {
            throw new TransicaoStatusInvalidaException(status, "cancelar");
        }
        this.status = StatusPedido.CANCELADO;
    }

    private void exigirRascunho(String acao) {
        if (status != StatusPedido.RASCUNHO) {
            throw new TransicaoStatusInvalidaException(status, acao);
        }
    }
}
