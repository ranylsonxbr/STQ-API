package com.example.Stq.produto.application.services;

import com.example.Stq.produto.application.dto.ProdutoCreateRequest;
import com.example.Stq.produto.application.dto.ProdutoDetalheResponse;
import com.example.Stq.produto.application.dto.ProdutoResponse;
import com.example.Stq.produto.application.dto.ProdutoUpdateRequest;
import com.example.Stq.produto.domain.Categoria;
import com.example.Stq.produto.domain.CategoriaRepository;
import com.example.Stq.produto.domain.Produto;
import com.example.Stq.produto.domain.ProdutoFiltro;
import com.example.Stq.produto.domain.ProdutoRepository;
import com.example.Stq.produto.domain.exception.CategoriaInativaException;
import com.example.Stq.produto.domain.exception.CategoriaNotFoundException;
import com.example.Stq.produto.domain.exception.ProdutoComPedidoEmAbertoException;
import com.example.Stq.produto.domain.exception.ProdutoNotFoundException;
import com.example.Stq.produto.domain.exception.SkuColisaoException;
import com.example.Stq.produto.domain.port.PedidoCompraReadPort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final PedidoCompraReadPort pedidoCompraReadPort;
    private final SkuGenerator skuGenerator;

    @Override
    @Transactional
    public ProdutoResponse criar(ProdutoCreateRequest request) {
        Categoria categoria = buscarCategoriaAtiva(request.categoriaId());

        Produto produto = Produto.builder()
                .nome(request.nome())
                .descricao(request.descricao())
                .categoria(categoria)
                .unidadeMedida(request.unidadeMedida())
                .estoqueMinimo(request.estoqueMinimo() != null ? request.estoqueMinimo() : 0)
                .build();

        produto = salvarComSkuRetry(produto);
        return ProdutoResponse.de(produto);
    }

    @Override
    @Transactional
    public ProdutoResponse atualizar(UUID id, ProdutoUpdateRequest request) {
        Produto produto = buscarOuLancar(id);
        Categoria categoria = buscarCategoriaAtiva(request.categoriaId());

        produto.setNome(request.nome());
        produto.setDescricao(request.descricao());
        produto.setCategoria(categoria);
        produto.setUnidadeMedida(request.unidadeMedida());
        produto.setEstoqueMinimo(request.estoqueMinimo() != null ? request.estoqueMinimo() : produto.getEstoqueMinimo());
        if (request.ativo() != null) {
            produto.setAtivo(request.ativo());
        }

        return ProdutoResponse.de(produtoRepository.save(produto));
    }

    @Override
    @Transactional
    public void desativar(UUID id) {
        Produto produto = buscarOuLancar(id);

        if (pedidoCompraReadPort.existePedidoEmAbertoParaProduto(id)) {
            throw new ProdutoComPedidoEmAbertoException();
        }

        produto.setAtivo(false);
        produtoRepository.save(produto);
    }

    @Override
    public ProdutoDetalheResponse buscarPorId(UUID id) {
        Produto produto = produtoRepository.findByIdComVariacoes(id)
                .orElseThrow(() -> new ProdutoNotFoundException(id));
        return ProdutoDetalheResponse.de(produto);
    }

    @Override
    public ProdutoDetalheResponse buscarPorSku(String sku) {
        Produto produto = produtoRepository.findBySku(sku)
                .orElseThrow(() -> new ProdutoNotFoundException(sku));
        return ProdutoDetalheResponse.de(produto);
    }

    @Override
    public Page<ProdutoResponse> listar(UUID categoriaId, Boolean ativo, String nome, Pageable pageable) {
        ProdutoFiltro filtro = new ProdutoFiltro(categoriaId, ativo, nome);
        return produtoRepository.findAll(filtro, pageable).map(ProdutoResponse::de);
    }

    private Produto buscarOuLancar(UUID id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoNotFoundException(id));
    }

    private Categoria buscarCategoriaAtiva(UUID categoriaId) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new CategoriaNotFoundException(categoriaId));
        if (!categoria.isAtivo()) {
            throw new CategoriaInativaException();
        }
        return categoria;
    }

    private Produto salvarComSkuRetry(Produto produto) {
        for (int i = 0; i < 5; i++) {
            try {
                produto.setSku(skuGenerator.gerarSkuProduto());
                return produtoRepository.save(produto);
            } catch (DataIntegrityViolationException e) {
                if (i == 4) throw new SkuColisaoException();
            }
        }
        throw new SkuColisaoException();
    }
}
