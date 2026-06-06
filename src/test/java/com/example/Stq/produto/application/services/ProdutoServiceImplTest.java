package com.example.Stq.produto.application.services;

import com.example.Stq.produto.application.dto.ProdutoCreateRequest;
import com.example.Stq.produto.application.dto.ProdutoUpdateRequest;
import com.example.Stq.produto.domain.*;
import com.example.Stq.produto.domain.exception.*;
import com.example.Stq.produto.domain.port.PedidoCompraReadPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProdutoService")
class ProdutoServiceImplTest {

    @Mock private ProdutoRepository produtoRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private PedidoCompraReadPort pedidoCompraReadPort;
    @Mock private SkuGenerator skuGenerator;
    @InjectMocks private ProdutoServiceImpl service;

    private Categoria categoriaAtiva() {
        return Categoria.builder().id(UUID.randomUUID()).nome("Cat").ativo(true).build();
    }

    private Produto produtoSalvo(Categoria cat) {
        return Produto.builder()
                .id(UUID.randomUUID()).sku("PRD-ABC123").nome("Produto Teste")
                .categoria(cat).unidadeMedida(UnidadeMedida.UN)
                .estoqueMinimo(0).ativo(true).criadoEm(Instant.now())
                .build();
    }

    @Nested
    @DisplayName("criar")
    class Criar {

        @Test
        @DisplayName("[PROD-C1] deve criar produto sem variacoes e retornar SKU gerado")
        void deveCriarProdutoComSkuGerado() {
            var cat = categoriaAtiva();
            var request = new ProdutoCreateRequest("Produto", null, cat.getId(), UnidadeMedida.UN, 0);
            var salvo = produtoSalvo(cat);

            when(categoriaRepository.findById(cat.getId())).thenReturn(Optional.of(cat));
            when(skuGenerator.gerarSkuProduto()).thenReturn("PRD-ABC123");
            when(produtoRepository.save(any())).thenReturn(salvo);

            var response = service.criar(request);

            assertThat(response.sku()).isEqualTo("PRD-ABC123");
            verify(produtoRepository).save(any());
        }

        @Test
        @DisplayName("[PROD-C2] deve ignorar campo SKU informado pelo cliente (nao esta no DTO)")
        void deveCriarSemSkuNoRequest() {
            var cat = categoriaAtiva();
            var request = new ProdutoCreateRequest("Produto", null, cat.getId(), UnidadeMedida.UN, 0);

            when(categoriaRepository.findById(cat.getId())).thenReturn(Optional.of(cat));
            when(skuGenerator.gerarSkuProduto()).thenReturn("PRD-SYS001");
            when(produtoRepository.save(any())).thenReturn(produtoSalvo(cat));

            var response = service.criar(request);
            assertThat(response.sku()).isNotBlank();
        }

        @Test
        @DisplayName("[PROD-C3] deve lancar CategoriaNotFoundException quando categoria inexistente")
        void deveLancarExcecaoCategoriaNaoEncontrada() {
            UUID catId = UUID.randomUUID();
            when(categoriaRepository.findById(catId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.criar(
                    new ProdutoCreateRequest("P", null, catId, UnidadeMedida.UN, 0)))
                    .isInstanceOf(CategoriaNotFoundException.class);
        }

        @Test
        @DisplayName("[EB-3] deve lancar CategoriaInativaException quando categoria inativa")
        void deveLancarExcecaoCategoriaInativa() {
            var catInativa = Categoria.builder().id(UUID.randomUUID()).nome("Inativa").ativo(false).build();
            when(categoriaRepository.findById(catInativa.getId())).thenReturn(Optional.of(catInativa));

            assertThatThrownBy(() -> service.criar(
                    new ProdutoCreateRequest("P", null, catInativa.getId(), UnidadeMedida.UN, 0)))
                    .isInstanceOf(CategoriaInativaException.class);
        }
    }

    @Nested
    @DisplayName("atualizar")
    class Atualizar {

        @Test
        @DisplayName("[PROD-C8] deve atualizar produto sem alterar SKU")
        void deveAtualizarSemAlterarSku() {
            var cat = categoriaAtiva();
            var produto = produtoSalvo(cat);
            var skuOriginal = produto.getSku();
            var request = new ProdutoUpdateRequest("Novo Nome", null, cat.getId(), UnidadeMedida.KG, 5, null);

            when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));
            when(categoriaRepository.findById(cat.getId())).thenReturn(Optional.of(cat));
            when(produtoRepository.save(any())).thenReturn(produto);

            service.atualizar(produto.getId(), request);

            assertThat(produto.getSku()).isEqualTo(skuOriginal);
            assertThat(produto.getNome()).isEqualTo("Novo Nome");
        }

        @Test
        @DisplayName("[EB-5] deve lancar CategoriaNotFoundException ao trocar para categoria inexistente")
        void deveLancarExcecaoCategoriaNaoEncontradaAoAtualizar() {
            var produto = produtoSalvo(categoriaAtiva());
            UUID novaCatId = UUID.randomUUID();

            when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));
            when(categoriaRepository.findById(novaCatId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizar(produto.getId(),
                    new ProdutoUpdateRequest("N", null, novaCatId, UnidadeMedida.UN, 0, null)))
                    .isInstanceOf(CategoriaNotFoundException.class);
        }

        @Test
        @DisplayName("[EB-6] deve permitir reativar produto inativo via PUT")
        void devePermitirReativarProdutoInativo() {
            var cat = categoriaAtiva();
            var produto = produtoSalvo(cat);
            produto.setAtivo(false);

            when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));
            when(categoriaRepository.findById(cat.getId())).thenReturn(Optional.of(cat));
            when(produtoRepository.save(any())).thenReturn(produto);

            service.atualizar(produto.getId(),
                    new ProdutoUpdateRequest("P", null, cat.getId(), UnidadeMedida.UN, 0, true));

            assertThat(produto.isAtivo()).isTrue();
        }
    }

    @Nested
    @DisplayName("desativar")
    class Desativar {

        @Test
        @DisplayName("[PROD-C9] deve desativar produto sem pedidos em aberto")
        void deveDesativarProdutoSemPedidos() {
            var produto = produtoSalvo(categoriaAtiva());

            when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));
            when(pedidoCompraReadPort.existePedidoEmAbertoParaProduto(produto.getId())).thenReturn(false);
            when(produtoRepository.save(any())).thenReturn(produto);

            service.desativar(produto.getId());

            assertThat(produto.isAtivo()).isFalse();
        }

        @Test
        @DisplayName("[PROD-C10] deve lancar ProdutoComPedidoEmAbertoException quando tem pedido (RN-07)")
        void deveLancarExcecaoComPedidoEmAberto() {
            var produto = produtoSalvo(categoriaAtiva());

            when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));
            when(pedidoCompraReadPort.existePedidoEmAbertoParaProduto(produto.getId())).thenReturn(true);

            assertThatThrownBy(() -> service.desativar(produto.getId()))
                    .isInstanceOf(ProdutoComPedidoEmAbertoException.class);
        }
    }

    @Nested
    @DisplayName("buscarPorId e buscarPorSku")
    class Buscar {

        @Test
        @DisplayName("[PROD-C6] deve retornar produto com variacoes por ID")
        void deveBuscarPorId() {
            var produto = produtoSalvo(categoriaAtiva());
            when(produtoRepository.findByIdComVariacoes(produto.getId())).thenReturn(Optional.of(produto));

            var response = service.buscarPorId(produto.getId());
            assertThat(response.id()).isEqualTo(produto.getId());
        }

        @Test
        @DisplayName("[PROD-C7] deve retornar produto por SKU")
        void deveBuscarPorSku() {
            var produto = produtoSalvo(categoriaAtiva());
            when(produtoRepository.findBySkuComVariacoes("PRD-ABC123")).thenReturn(Optional.of(produto));

            var response = service.buscarPorSku("PRD-ABC123");
            assertThat(response.sku()).isEqualTo("PRD-ABC123");
        }

        @Test
        @DisplayName("deve lancar ProdutoNotFoundException quando nao encontrado por ID")
        void deveLancarExcecaoNaoEncontradoPorId() {
            UUID id = UUID.randomUUID();
            when(produtoRepository.findByIdComVariacoes(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorId(id))
                    .isInstanceOf(ProdutoNotFoundException.class);
        }

        @Test
        @DisplayName("deve lancar ProdutoNotFoundException quando nao encontrado por SKU")
        void deveLancarExcecaoNaoEncontradoPorSku() {
            when(produtoRepository.findBySkuComVariacoes("PRD-INVALIDO")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorSku("PRD-INVALIDO"))
                    .isInstanceOf(ProdutoNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("listar")
    class Listar {

        @Test
        @DisplayName("[PROD-C5] deve listar produtos paginados com filtros")
        void deveListarProdutosPaginados() {
            var page = new PageImpl<>(List.of(produtoSalvo(categoriaAtiva())));
            when(produtoRepository.findAll(any(ProdutoFiltro.class), any())).thenReturn(page);

            var resultado = service.listar(null, null, null, PageRequest.of(0, 20));
            assertThat(resultado.getContent()).hasSize(1);
        }
    }
}
