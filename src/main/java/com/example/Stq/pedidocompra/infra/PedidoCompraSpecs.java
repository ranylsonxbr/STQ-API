package com.example.Stq.pedidocompra.infra;

import com.example.Stq.pedidocompra.domain.PedidoCompra;
import com.example.Stq.pedidocompra.domain.PedidoCompraFiltro;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class PedidoCompraSpecs {
    private PedidoCompraSpecs() {}

    public static Specification<PedidoCompra> comFiltro(PedidoCompraFiltro f) {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("fornecedor", JoinType.LEFT);
            }
            List<Predicate> ps = new ArrayList<>();
            if (f.fornecedorId() != null) {
                ps.add(cb.equal(root.get("fornecedor").get("id"), f.fornecedorId()));
            }
            if (f.status() != null) {
                ps.add(cb.equal(root.get("status"), f.status()));
            }
            if (f.de() != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("dataEmissao"), f.de()));
            }
            if (f.ate() != null) {
                ps.add(cb.lessThanOrEqualTo(root.get("dataEmissao"), f.ate()));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
}
