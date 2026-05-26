package com.example.Stq.produto.application.services;

import com.example.Stq.produto.application.dto.CategoriaCreateRequest;
import com.example.Stq.produto.application.dto.CategoriaResponse;
import com.example.Stq.produto.domain.Categoria;
import com.example.Stq.produto.domain.CategoriaRepository;
import com.example.Stq.produto.domain.ProdutoRepository;
import com.example.Stq.produto.domain.exception.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoriaService")
class CategoriaServiceImplTest {

    @Mock private CategoriaRepository categoriaRepository;
    @Mock private ProdutoRepository produtoRepository;
    @InjectMocks private CategoriaServiceImpl service;

    @Nested
    @DisplayName("criar")
    class Criar {

        @Test
        @DisplayName("[US-A1] deve criar categoria raiz com sucesso")
        void deveCriarCategoriaRaiz() {
            var request = new CategoriaCreateRequest("Eletrônicos", null);
            var salva = Categoria.builder().id(UUID.randomUUID()).nome("Eletrônicos").build();

            when(categoriaRepository.existsByNomeAndCategoriaPaiIsNull("Eletrônicos")).thenReturn(false);
            when(categoriaRepository.save(any())).thenReturn(salva);

            CategoriaResponse response = service.criar(request);

            assertThat(response.nome()).isEqualTo("Eletrônicos");
            assertThat(response.categoriaPaiId()).isNull();
        }

        @Test
        @DisplayName("[US-A2] deve criar subcategoria com pai ativo")
        void deveCriarSubcategoria() {
            UUID paiId = UUID.randomUUID();
            var pai = Categoria.builder().id(paiId).nome("Pai").ativo(true).build();
            var request = new CategoriaCreateRequest("Filho", paiId);
            var salva = Categoria.builder().id(UUID.randomUUID()).nome("Filho").categoriaPai(pai).build();

            when(categoriaRepository.findById(paiId)).thenReturn(Optional.of(pai));
            when(categoriaRepository.existsByNomeAndCategoriaPaiId("Filho", paiId)).thenReturn(false);
            when(categoriaRepository.save(any())).thenReturn(salva);

            CategoriaResponse response = service.criar(request);

            assertThat(response.categoriaPaiId()).isEqualTo(paiId);
        }

        @Test
        @DisplayName("[EB-2] deve lancar CategoriaInativaException ao usar pai inativo")
        void deveLancarExcecaoComPaiInativo() {
            UUID paiId = UUID.randomUUID();
            var paiInativo = Categoria.builder().id(paiId).nome("Pai").ativo(false).build();

            when(categoriaRepository.findById(paiId)).thenReturn(Optional.of(paiInativo));
            when(categoriaRepository.existsByNomeAndCategoriaPaiId(any(), any())).thenReturn(false);

            assertThatThrownBy(() -> service.criar(new CategoriaCreateRequest("Filho", paiId)))
                    .isInstanceOf(CategoriaInativaException.class);
        }

        @Test
        @DisplayName("deve lancar CategoriaNomeDuplicadoException para nome duplicado no nivel raiz")
        void deveLancarExcecaoNomeDuplicadoRaiz() {
            when(categoriaRepository.existsByNomeAndCategoriaPaiIsNull("Dup")).thenReturn(true);

            assertThatThrownBy(() -> service.criar(new CategoriaCreateRequest("Dup", null)))
                    .isInstanceOf(CategoriaNomeDuplicadoException.class);
        }

        @Test
        @DisplayName("deve lancar CategoriaNotFoundException ao criar com pai inexistente")
        void deveLancarExcecaoComPaiInexistente() {
            UUID paiId = UUID.randomUUID();
            when(categoriaRepository.findById(paiId)).thenReturn(Optional.empty());
            when(categoriaRepository.existsByNomeAndCategoriaPaiId(any(), eq(paiId))).thenReturn(false);

            assertThatThrownBy(() -> service.criar(new CategoriaCreateRequest("Filho", paiId)))
                    .isInstanceOf(CategoriaNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("desativar")
    class Desativar {

        @Test
        @DisplayName("[US-A5] deve desativar categoria sem filhas nem produtos")
        void deveDesativarCategoria() {
            UUID id = UUID.randomUUID();
            var cat = Categoria.builder().id(id).nome("Vazia").ativo(true).build();

            when(categoriaRepository.findById(id)).thenReturn(Optional.of(cat));
            when(categoriaRepository.existsByCategoriaPaiIdAndAtivoTrue(id)).thenReturn(false);
            when(produtoRepository.existsByCategoria_IdAndAtivoTrue(id)).thenReturn(false);
            when(categoriaRepository.save(any())).thenReturn(cat);

            service.desativar(id);

            assertThat(cat.isAtivo()).isFalse();
        }

        @Test
        @DisplayName("[EB-10] deve lancar CategoriaComFilhasException quando tem filhas ativas")
        void deveLancarExcecaoComFilhasAtivas() {
            UUID id = UUID.randomUUID();
            var cat = Categoria.builder().id(id).nome("Com filhas").ativo(true).build();

            when(categoriaRepository.findById(id)).thenReturn(Optional.of(cat));
            when(categoriaRepository.existsByCategoriaPaiIdAndAtivoTrue(id)).thenReturn(true);

            assertThatThrownBy(() -> service.desativar(id))
                    .isInstanceOf(CategoriaComFilhasException.class);
        }

        @Test
        @DisplayName("[US-A5] deve lancar CategoriaComProdutosException quando tem produtos ativos")
        void deveLancarExcecaoComProdutosAtivos() {
            UUID id = UUID.randomUUID();
            var cat = Categoria.builder().id(id).nome("Com produtos").ativo(true).build();

            when(categoriaRepository.findById(id)).thenReturn(Optional.of(cat));
            when(categoriaRepository.existsByCategoriaPaiIdAndAtivoTrue(id)).thenReturn(false);
            when(produtoRepository.existsByCategoria_IdAndAtivoTrue(id)).thenReturn(true);

            assertThatThrownBy(() -> service.desativar(id))
                    .isInstanceOf(CategoriaComProdutosException.class);
        }
    }

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar categoria quando encontrada")
        void deveRetornarCategoria() {
            UUID id = UUID.randomUUID();
            var cat = Categoria.builder().id(id).nome("Teste").build();
            when(categoriaRepository.findById(id)).thenReturn(Optional.of(cat));

            CategoriaResponse response = service.buscarPorId(id);

            assertThat(response.id()).isEqualTo(id);
        }

        @Test
        @DisplayName("deve lancar CategoriaNotFoundException quando nao encontrada")
        void deveLancarExcecaoQuandoNaoEncontrada() {
            UUID id = UUID.randomUUID();
            when(categoriaRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorId(id))
                    .isInstanceOf(CategoriaNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("listar")
    class Listar {

        @Test
        @DisplayName("[US-A3] deve retornar pagina de categorias")
        void deveListarCategorias() {
            var page = new PageImpl<>(List.of(
                    Categoria.builder().id(UUID.randomUUID()).nome("A").build()
            ));
            when(categoriaRepository.findAll(isNull(), isNull(), any())).thenReturn(page);

            var resultado = service.listar(null, null, PageRequest.of(0, 20));

            assertThat(resultado.getContent()).hasSize(1);
        }
    }
}
