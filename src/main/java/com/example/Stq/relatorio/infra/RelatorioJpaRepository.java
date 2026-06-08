package com.example.Stq.relatorio.infra;

import com.example.Stq.movimentacao.domain.StatusEstoque;
import com.example.Stq.movimentacao.domain.TipoMovimentacao;
import com.example.Stq.pedidocompra.domain.StatusPedido;
import com.example.Stq.relatorio.application.dto.EstoqueAtualItem;
import com.example.Stq.relatorio.application.dto.MovimentacaoItem;
import com.example.Stq.relatorio.application.dto.PedidoCompraItem;
import com.example.Stq.relatorio.application.dto.TotalPorTipo;
import com.example.Stq.relatorio.domain.RelatorioRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RelatorioJpaRepository implements RelatorioRepository {

    private final EntityManager em;

    @Override
    public List<EstoqueAtualItem> estoqueAtual() {
        List<Object[]> rows = em.createQuery("""
                SELECT p.sku, p.nome, CAST(p.unidadeMedida AS string),
                       CAST(SUM(e.saldoAtual) AS int), p.estoqueMinimo
                FROM Estoque e JOIN e.produto p
                WHERE p.ativo = true
                GROUP BY p.id, p.sku, p.nome, p.unidadeMedida, p.estoqueMinimo
                ORDER BY p.nome
                """, Object[].class).getResultList();
        return rows.stream().map(this::toEstoqueAtualItem).toList();
    }

    @Override
    public List<EstoqueAtualItem> alertas() {
        List<Object[]> rows = em.createQuery("""
                SELECT p.sku, p.nome, CAST(p.unidadeMedida AS string),
                       CAST(SUM(e.saldoAtual) AS int), p.estoqueMinimo
                FROM Estoque e JOIN e.produto p
                WHERE p.ativo = true
                GROUP BY p.id, p.sku, p.nome, p.unidadeMedida, p.estoqueMinimo
                HAVING SUM(e.saldoAtual) <= p.estoqueMinimo
                ORDER BY p.nome
                """, Object[].class).getResultList();
        return rows.stream().map(this::toEstoqueAtualItem).toList();
    }

    @Override
    public List<MovimentacaoItem> movimentacoesPorPeriodo(LocalDate de, LocalDate ate, TipoMovimentacao tipo) {
        Instant instDe = de != null ? de.atStartOfDay(ZoneOffset.UTC).toInstant() : null;
        Instant instAte = ate != null ? ate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant() : null;

        List<Object[]> rows = em.createQuery("""
                SELECT m.produto.sku, m.produto.nome,
                       CAST(m.tipo AS string), CAST(m.origem AS string),
                       m.quantidade, m.realizadoEm
                FROM Movimentacao m JOIN m.produto p
                WHERE (:de IS NULL OR m.realizadoEm >= :de)
                  AND (:ate IS NULL OR m.realizadoEm < :ate)
                  AND (:tipo IS NULL OR m.tipo = :tipo)
                ORDER BY m.realizadoEm DESC
                """, Object[].class)
                .setParameter("de", instDe)
                .setParameter("ate", instAte)
                .setParameter("tipo", tipo)
                .getResultList();

        return rows.stream().map(r -> new MovimentacaoItem(
                (String) r[0], (String) r[1], (String) r[2], (String) r[3],
                (Integer) r[4], (Instant) r[5]
        )).toList();
    }

    @Override
    public List<TotalPorTipo> totaisPorTipo(LocalDate de, LocalDate ate, TipoMovimentacao tipo) {
        Instant instDe = de != null ? de.atStartOfDay(ZoneOffset.UTC).toInstant() : null;
        Instant instAte = ate != null ? ate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant() : null;

        List<Object[]> rows = em.createQuery("""
                SELECT CAST(m.tipo AS string), SUM(m.quantidade)
                FROM Movimentacao m
                WHERE (:de IS NULL OR m.realizadoEm >= :de)
                  AND (:ate IS NULL OR m.realizadoEm < :ate)
                  AND (:tipo IS NULL OR m.tipo = :tipo)
                GROUP BY m.tipo
                """, Object[].class)
                .setParameter("de", instDe)
                .setParameter("ate", instAte)
                .setParameter("tipo", tipo)
                .getResultList();

        return rows.stream().map(r -> new TotalPorTipo((String) r[0], (Long) r[1])).toList();
    }

    @Override
    public List<PedidoCompraItem> pedidosPorPeriodo(LocalDate de, LocalDate ate, StatusPedido status) {
        List<Object[]> rows = em.createQuery("""
                SELECT pc.id, pc.fornecedor.razaoSocial, CAST(pc.status AS string),
                       pc.dataEmissao, pc.totalPedido
                FROM PedidoCompra pc JOIN pc.fornecedor f
                WHERE (:de IS NULL OR pc.dataEmissao >= :de)
                  AND (:ate IS NULL OR pc.dataEmissao <= :ate)
                  AND (:status IS NULL OR pc.status = :status)
                ORDER BY pc.dataEmissao DESC
                """, Object[].class)
                .setParameter("de", de)
                .setParameter("ate", ate)
                .setParameter("status", status)
                .getResultList();

        return rows.stream().map(r -> new PedidoCompraItem(
                (java.util.UUID) r[0], (String) r[1], (String) r[2],
                (LocalDate) r[3], (BigDecimal) r[4]
        )).toList();
    }

    @Override
    public BigDecimal totalGasto(LocalDate de, LocalDate ate) {
        Object result = em.createQuery("""
                SELECT COALESCE(SUM(pc.totalPedido), 0)
                FROM PedidoCompra pc
                WHERE pc.status = :recebido
                  AND (:de IS NULL OR pc.dataEmissao >= :de)
                  AND (:ate IS NULL OR pc.dataEmissao <= :ate)
                """)
                .setParameter("recebido", StatusPedido.RECEBIDO)
                .setParameter("de", de)
                .setParameter("ate", ate)
                .getSingleResult();
        return result instanceof BigDecimal bd ? bd : new BigDecimal(result.toString());
    }

    private EstoqueAtualItem toEstoqueAtualItem(Object[] r) {
        int saldo = (Integer) r[3];
        int minimo = (Integer) r[4];
        return new EstoqueAtualItem(
                (String) r[0], (String) r[1], (String) r[2],
                saldo, minimo, StatusEstoque.calcular(saldo, minimo)
        );
    }
}
