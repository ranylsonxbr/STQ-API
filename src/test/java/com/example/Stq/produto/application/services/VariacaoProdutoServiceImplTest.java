package com.example.Stq.produto.application.services;

import com.example.Stq.produto.application.dto.VariacaoCreateRequest;
import com.example.Stq.produto.domain.*;
import com.example.Stq.produto.domain.exception.ProdutoNotFoundException;
import com.example.Stq.produto.domain.exception.VariacaoDuplicadaException;
import com.example.Stq.produto.domain.exception.VariacaoNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VariacaoProdutoService")
class VariacaoProdutoServiceImplTest {

    @Mock private ProdutoRepository produtoRepository;
    @Mock private VariacaoProdutoRepository variacaoProdutoRepository;
    @Mock private SkuGenerator skuGenerator;
    @InjectMocks private VariacaoProdutoServiceImpl service;

    private Produto produto() {
        Categoria cat = Categoria.builder().id(UUID.randomUUID()).nome("Cat").ativo(true).build();
        return Produto.builder()
                .id(UUID.randomUUID()).sku("PRD-ABC123").nome("Produto Teste")
                .categoria(cat).unidadeMedida(UnidadeMedida.UN)
                .estoqueMinimo(0).ativo(true).criadoEm(Instant.now())
                .build();
    }

    private VariacaoProduto variacaoSalva(Produto p) {
        return VariacaoProduto.builder()
                .id(UUID.randomUUID()).produto(p)
                .atributo("Cor").valor("Vermelho")
                .skuVariacao("PRD-ABC123-XY1").ativo(true)
                .build();
    }

    @Nested
    @DisplayName("adicionar")
    class Adicionar {

        @Test
        @DisplayName("[VAR-C1] deve adicionar variacao e retornar SKU derivado do produto")
        void deveAdicionarVariacaoComSkuDerivado() {
            var p = produto();
            var request = new VariacaoCreateRequest("Cor", "Vermelho");
            var salva = variacaoSalva(p);

            when(produtoRepository.findById(p.getId())).thenReturn(Optional.of(p));
            when(variacaoProdutoRepository.existsByProdutoIdAndAtributoAndValor(p.getId(), "Cor", "Vermelho")).thenReturn(false);
            when(skuGenerator.gerarSkuVariacao("PRD-ABC123")).thenReturn("PRD-ABC123-XY1");
            when(variacaoProdutoRepository.save(any())).thenReturn(salva);

            var response = service.adicionar(p.getId(), request);

            assertThat(response.skuVariacao()).startsWith("PRD-ABC123-");
            verify(variacaoProdutoRepository).save(any());
        }

        @Test
        @DisplayName("[VAR-C2] deve lancar ProdutoNotFoundException quando produto inexistente")
        void deveLancarExcecaoProdutoNaoEncontrado() {
            UUID produtoId = UUID.randomUUID();
            when(produtoRepository.findById(produtoId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.adicionar(produtoId, new VariacaoCreateRequest("Cor", "Azul")))
                    .isInstanceOf(ProdutoNotFoundException.class);
        }

        @Test
        @DisplayName("[VAR-C3] deve lancar VariacaoDuplicadaException para atributo+valor duplicado")
        void deveLancarExcecaoVariacaoDuplicada() {
            var p = produto();
            when(produtoRepository.findById(p.getId())).thenReturn(Optional.of(p));
            when(variacaoProdutoRepository.existsByProdutoIdAndAtributoAndValor(p.getId(), "Cor", "Azul")).thenReturn(true);

            assertThatThrownBy(() -> service.adicionar(p.getId(), new VariacaoCreateRequest("Cor", "Azul")))
                    .isInstanceOf(VariacaoDuplicadaException.class);
        }

        @Test
        @DisplayName("deve chamar skuGenerator com o SKU do produto pai")
        void deveChamarSkuGeneratorComSkuProduto() {
            var p = produto();
            var request = new VariacaoCreateRequest("Tamanho", "M");

            when(produtoRepository.findById(p.getId())).thenReturn(Optional.of(p));
            when(variacaoProdutoRepository.existsByProdutoIdAndAtributoAndValor(any(), any(), any())).thenReturn(false);
            when(skuGenerator.gerarSkuVariacao("PRD-ABC123")).thenReturn("PRD-ABC123-M01");
            when(variacaoProdutoRepository.save(any())).thenReturn(variacaoSalva(p));

            service.adicionar(p.getId(), request);

            verify(skuGenerator).gerarSkuVariacao("PRD-ABC123");
        }
    }

    @Nested
    @DisplayName("desativar")
    class Desativar {

        @Test
        @DisplayName("[VAR-C4] deve desativar variacao existente")
        void deveDesativarVariacao() {
            var p = produto();
            var variacao = variacaoSalva(p);

            when(variacaoProdutoRepository.findByIdAndProdutoId(variacao.getId(), p.getId()))
                    .thenReturn(Optional.of(variacao));
            when(variacaoProdutoRepository.save(any())).thenReturn(variacao);

            service.desativar(p.getId(), variacao.getId());

            assertThat(variacao.isAtivo()).isFalse();
            verify(variacaoProdutoRepository).save(variacao);
        }

        @Test
        @DisplayName("[VAR-C5] deve lancar VariacaoNotFoundException quando variacao nao pertence ao produto")
        void deveLancarExcecaoVariacaoNaoEncontrada() {
            UUID produtoId = UUID.randomUUID();
            UUID variacaoId = UUID.randomUUID();
            when(variacaoProdutoRepository.findByIdAndProdutoId(variacaoId, produtoId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.desativar(produtoId, variacaoId))
                    .isInstanceOf(VariacaoNotFoundException.class);
        }
    }
}
