package com.example.Stq.produto.application.services;

import com.example.Stq.produto.application.dto.CategoriaCreateRequest;
import com.example.Stq.produto.application.dto.CategoriaResponse;
import com.example.Stq.produto.application.dto.CategoriaUpdateRequest;
import com.example.Stq.produto.domain.Categoria;
import com.example.Stq.produto.domain.CategoriaRepository;
import com.example.Stq.produto.domain.ProdutoRepository;
import com.example.Stq.produto.domain.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;

    @Override
    @Transactional
    public CategoriaResponse criar(CategoriaCreateRequest request) {
        validarNomePorNivel(request.nome(), request.categoriaPaiId(), null);

        Categoria pai = null;
        if (request.categoriaPaiId() != null) {
            pai = categoriaRepository.findById(request.categoriaPaiId())
                    .orElseThrow(() -> new CategoriaNotFoundException(request.categoriaPaiId()));
            if (!pai.isAtivo()) {
                throw new CategoriaInativaException();
            }
        }

        Categoria categoria = Categoria.builder()
                .nome(request.nome())
                .categoriaPai(pai)
                .build();

        return CategoriaResponse.de(categoriaRepository.save(categoria));
    }

    @Override
    @Transactional
    public CategoriaResponse atualizar(UUID id, CategoriaUpdateRequest request) {
        Categoria categoria = buscarOuLancar(id);
        validarNomePorNivel(request.nome(), request.categoriaPaiId(), id);

        Categoria pai = null;
        if (request.categoriaPaiId() != null) {
            pai = categoriaRepository.findById(request.categoriaPaiId())
                    .orElseThrow(() -> new CategoriaNotFoundException(request.categoriaPaiId()));
            if (!pai.isAtivo()) {
                throw new CategoriaInativaException();
            }
        }

        categoria.setNome(request.nome());
        categoria.setCategoriaPai(pai);

        return CategoriaResponse.de(categoriaRepository.save(categoria));
    }

    @Override
    @Transactional
    public void desativar(UUID id) {
        Categoria categoria = buscarOuLancar(id);

        if (categoriaRepository.existsByCategoriaPaiIdAndAtivoTrue(id)) {
            throw new CategoriaComFilhasException();
        }
        if (produtoRepository.existsByCategoria_IdAndAtivoTrue(id)) {
            throw new CategoriaComProdutosException();
        }

        categoria.setAtivo(false);
        categoriaRepository.save(categoria);
    }

    @Override
    public CategoriaResponse buscarPorId(UUID id) {
        return CategoriaResponse.de(buscarOuLancar(id));
    }

    @Override
    public Page<CategoriaResponse> listar(UUID categoriaPaiId, Boolean ativo, Pageable pageable) {
        return categoriaRepository.findAll(categoriaPaiId, ativo, pageable)
                .map(CategoriaResponse::de);
    }

    private Categoria buscarOuLancar(UUID id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException(id));
    }

    private void validarNomePorNivel(String nome, UUID categoriaPaiId, UUID idAtual) {
        boolean duplicado;
        if (idAtual == null) {
            duplicado = categoriaPaiId == null
                    ? categoriaRepository.existsByNomeAndCategoriaPaiIsNull(nome)
                    : categoriaRepository.existsByNomeAndCategoriaPaiId(nome, categoriaPaiId);
        } else {
            duplicado = categoriaPaiId == null
                    ? categoriaRepository.existsByNomeAndCategoriaPaiIsNullAndIdNot(nome, idAtual)
                    : categoriaRepository.existsByNomeAndCategoriaPaiIdAndIdNot(nome, categoriaPaiId, idAtual);
        }

        if (duplicado) {
            throw new CategoriaNomeDuplicadoException();
        }
    }
}
