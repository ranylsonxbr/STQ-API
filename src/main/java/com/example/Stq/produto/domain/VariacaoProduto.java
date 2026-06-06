package com.example.Stq.produto.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "variacao_produto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariacaoProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false, length = 60)
    private String atributo;

    @Column(nullable = false, length = 120)
    private String valor;

    @Column(name = "sku_variacao", nullable = false, unique = true, length = 40, updatable = false)
    private String skuVariacao;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;
}
