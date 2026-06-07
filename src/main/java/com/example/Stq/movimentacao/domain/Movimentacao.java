package com.example.Stq.movimentacao.domain;

import com.example.Stq.autenticacao.domain.Usuario;
import com.example.Stq.produto.domain.Produto;
import com.example.Stq.produto.domain.VariacaoProduto;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "movimentacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false, updatable = false)
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variacao_id", updatable = false)
    private VariacaoProduto variacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15, updatable = false)
    private TipoMovimentacao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private OrigemMovimentacao origem;

    @Column(nullable = false, updatable = false)
    private Integer quantidade;

    @Column(name = "saldo_antes", nullable = false, updatable = false)
    private Integer saldoAntes;

    @Column(name = "saldo_depois", nullable = false, updatable = false)
    private Integer saldoDepois;

    @Column(length = 500, updatable = false)
    private String observacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "realizado_por", nullable = false, updatable = false)
    private Usuario realizadoPor;

    @Column(name = "realizado_em", nullable = false, updatable = false)
    private Instant realizadoEm;

    @PrePersist
    void prePersist() {
        realizadoEm = Instant.now();
    }
}
