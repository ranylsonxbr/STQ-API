package com.example.Stq.fornecedor.infra;

import com.example.Stq.fornecedor.domain.CnpjValidator;
import com.example.Stq.fornecedor.domain.Fornecedor;
import com.example.Stq.fornecedor.domain.FornecedorFiltro;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class FornecedorSpecs {
    private FornecedorSpecs() {}

    public static Specification<Fornecedor> comFiltro(FornecedorFiltro f) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (f.razaoSocial() != null && !f.razaoSocial().isBlank()) {
                ps.add(cb.like(cb.lower(root.get("razaoSocial")),
                        "%" + f.razaoSocial().toLowerCase() + "%"));
            }
            if (f.cnpj() != null && !f.cnpj().isBlank()) {
                ps.add(cb.equal(root.get("cnpj"),
                        CnpjValidator.normalizar(f.cnpj())));
            }
            Boolean ativo = f.ativo() == null ? Boolean.TRUE : f.ativo();
            ps.add(cb.equal(root.get("ativo"), ativo));
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
}
