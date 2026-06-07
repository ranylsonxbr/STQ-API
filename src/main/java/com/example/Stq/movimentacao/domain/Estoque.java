package com.example.Stq.movimentacao.domain;

import com.example.Stq.produto.domain.Produto;
import com.example.Stq.produto.domain.VariacaoProduto;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "estoque",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_estoque_produto_variacao_localizacao",
        columnNames = {"produto_id", "variacao_id", "localizacao"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variacao_id")
    private VariacaoProduto variacao;

    @Column(length = 60)
    private String localizacao;

    @Column(name = "saldo_atual", nullable = false)
    @Builder.Default
    private Integer saldoAtual = 0;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @PrePersist
    @PreUpdate
    void preSave() {
        atualizadoEm = Instant.now();
    }
}
