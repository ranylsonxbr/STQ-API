package com.example.Stq.produto.infra;

import com.example.Stq.produto.domain.Categoria;
import com.example.Stq.produto.domain.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CategoriaRepositoryImpl implements CategoriaRepository {

    private final CategoriaJpaRepository jpa;

    @Override
    public Optional<Categoria> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public boolean existsByNomeAndCategoriaPaiId(String nome, UUID categoriaPaiId) {
        return jpa.existsByNomeAndCategoriaPai_Id(nome, categoriaPaiId);
    }

    @Override
    public boolean existsByNomeAndCategoriaPaiIsNull(String nome) {
        return jpa.existsByNomeAndCategoriaPaiIsNull(nome);
    }

    @Override
    public boolean existsByCategoriaPaiId(UUID categoriaPaiId) {
        return jpa.existsByCategoriaPai_Id(categoriaPaiId);
    }

    @Override
    public boolean existsByCategoriaPaiIdAndAtivoTrue(UUID categoriaPaiId) {
        return jpa.existsByCategoriaPai_IdAndAtivoTrue(categoriaPaiId);
    }

    @Override
    public Page<Categoria> findAll(UUID categoriaPaiId, Boolean ativo, Pageable pageable) {
        Specification<Categoria> spec = (root, query, cb) -> cb.conjunction();

        if (categoriaPaiId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("categoriaPai").get("id"), categoriaPaiId));
        }
        if (ativo != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("ativo"), ativo));
        }

        return jpa.findAll(spec, pageable);
    }

    @Override
    public Categoria save(Categoria categoria) {
        return jpa.save(categoria);
    }
}
