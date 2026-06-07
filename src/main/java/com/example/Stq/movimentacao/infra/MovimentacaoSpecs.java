package com.example.Stq.movimentacao.infra;

import com.example.Stq.movimentacao.domain.Movimentacao;
import com.example.Stq.movimentacao.domain.MovimentacaoFiltro;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class MovimentacaoSpecs {
    private MovimentacaoSpecs() {}

    public static Specification<Movimentacao> comFiltro(MovimentacaoFiltro f) {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("produto", JoinType.LEFT);
                root.fetch("variacao", JoinType.LEFT);
                root.fetch("realizadoPor", JoinType.LEFT);
            }
            List<Predicate> ps = new ArrayList<>();
            if (f.produtoId() != null) {
                ps.add(cb.equal(root.get("produto").get("id"), f.produtoId()));
            }
            if (f.tipo() != null) {
                ps.add(cb.equal(root.get("tipo"), f.tipo()));
            }
            if (f.de() != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("realizadoEm"), f.de()));
            }
            if (f.ate() != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("realizadoEm"), f.ate()));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
}
