package com.example.Stq.produto.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "produto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 20, updatable = false)
    private String sku;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 1000)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 4)
    private UnidadeMedida unidadeMedida;

    @Column(nullable = false)
    @Builder.Default
    private Integer estoqueMinimo = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;

    @Column(nullable = false, updatable = false)
    private Instant criadoEm;

    @OneToMany(mappedBy = "produto", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = false)
    @Builder.Default
    private List<VariacaoProduto> variacoes = new ArrayList<>();

    @PrePersist
    void prePersist() {
        criadoEm = Instant.now();
    }
}
