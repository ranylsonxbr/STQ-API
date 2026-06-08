package com.example.Stq.produto.application.services;

import com.example.Stq.produto.application.dto.VariacaoCreateRequest;
import com.example.Stq.produto.application.dto.VariacaoResponse;
import com.example.Stq.produto.domain.Produto;
import com.example.Stq.produto.domain.ProdutoRepository;
import com.example.Stq.produto.domain.VariacaoProduto;
import com.example.Stq.produto.domain.VariacaoProdutoRepository;

import java.util.List;
import com.example.Stq.produto.domain.exception.ProdutoNotFoundException;
import com.example.Stq.produto.domain.exception.VariacaoDuplicadaException;
import com.example.Stq.produto.domain.exception.VariacaoNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VariacaoProdutoServiceImpl implements VariacaoProdutoService {

    private final ProdutoRepository produtoRepository;
    private final VariacaoProdutoRepository variacaoProdutoRepository;
    private final SkuGenerator skuGenerator;

    @Override
    @Transactional
    public VariacaoResponse adicionar(UUID produtoId, VariacaoCreateRequest request) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ProdutoNotFoundException(produtoId));

        if (variacaoProdutoRepository.existsByProdutoIdAndAtributoAndValor(
                produtoId, request.atributo(), request.valor())) {
            throw new VariacaoDuplicadaException(request.atributo(), request.valor());
        }

        String skuVariacao = skuGenerator.gerarSkuVariacao(produto.getSku());

        VariacaoProduto variacao = VariacaoProduto.builder()
                .produto(produto)
                .atributo(request.atributo())
                .valor(request.valor())
                .skuVariacao(skuVariacao)
                .build();

        return VariacaoResponse.de(variacaoProdutoRepository.save(variacao));
    }

    @Override
    @Transactional
    public void desativar(UUID produtoId, UUID variacaoId) {
        VariacaoProduto variacao = variacaoProdutoRepository.findByIdAndProdutoId(variacaoId, produtoId)
                .orElseThrow(() -> new VariacaoNotFoundException(variacaoId));

        variacao.setAtivo(false);
        variacaoProdutoRepository.save(variacao);
    }

    @Override
    public List<VariacaoResponse> listarAtivasPorProduto(UUID produtoId) {
        return variacaoProdutoRepository.findAtivasByProdutoId(produtoId)
                .stream()
                .map(VariacaoResponse::de)
                .toList();
    }
}
