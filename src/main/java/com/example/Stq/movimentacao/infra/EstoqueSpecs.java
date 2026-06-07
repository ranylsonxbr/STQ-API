package com.example.Stq.movimentacao.infra;

import com.example.Stq.movimentacao.domain.Estoque;
import com.example.Stq.movimentacao.domain.EstoqueFiltro;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class EstoqueSpecs {
    private EstoqueSpecs() {}

    public static Specification<Estoque> comFiltro(EstoqueFiltro f) {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("produto", JoinType.LEFT);
                root.fetch("variacao", JoinType.LEFT);
            }
            List<Predicate> ps = new ArrayList<>();
            if (f.produtoId() != null) {
                ps.add(cb.equal(root.get("produto").get("id"), f.produtoId()));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
}
