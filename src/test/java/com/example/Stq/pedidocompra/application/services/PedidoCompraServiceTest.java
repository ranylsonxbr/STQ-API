package com.example.Stq.pedidocompra.application.services;

import com.example.Stq.autenticacao.domain.Usuario;
import com.example.Stq.autenticacao.domain.UsuarioRepository;
import com.example.Stq.fornecedor.domain.Fornecedor;
import com.example.Stq.fornecedor.domain.FornecedorRepository;
import com.example.Stq.fornecedor.domain.exception.FornecedorNotFoundException;
import com.example.Stq.movimentacao.application.dto.EntradaPedidoComando;
import com.example.Stq.movimentacao.application.services.MovimentacaoService;
import com.example.Stq.pedidocompra.application.dto.CriarPedidoRequest;
import com.example.Stq.pedidocompra.application.dto.ItemRequest;
import com.example.Stq.pedidocompra.application.dto.PedidoCompraResponse;
import com.example.Stq.pedidocompra.domain.ItemPedidoCompra;
import com.example.Stq.pedidocompra.domain.PedidoCompra;
import com.example.Stq.pedidocompra.domain.StatusPedido;
import com.example.Stq.pedidocompra.domain.exception.FornecedorInativoException;
import com.example.Stq.pedidocompra.domain.exception.ItemDuplicadoException;
import com.example.Stq.pedidocompra.domain.exception.PedidoCompraNotFoundException;
import com.example.Stq.pedidocompra.domain.exception.PedidoSemItensException;
import com.example.Stq.pedidocompra.domain.exception.TransicaoStatusInvalidaException;
import com.example.Stq.produto.domain.Produto;
import com.example.Stq.produto.domain.ProdutoRepository;
import com.example.Stq.produto.domain.VariacaoProdutoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PedidoCompraServiceImpl")
class PedidoCompraServiceTest {

    private static final UUID FORNECEDOR_ID = UUID.fromString("00000000-0000-0000-0000-0000000000f1");
    private static final UUID USUARIO_ID = UUID.fromString("00000000-0000-0000-0000-0000000000b2");
    private static final UUID PRODUTO_ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final UUID PRODUTO_ID_2 = UUID.fromString("00000000-0000-0000-0000-0000000000a2");
    private static final UUID PEDIDO_ID = UUID.fromString("00000000-0000-0000-0000-0000000000d1");

    @Mock private com.example.Stq.pedidocompra.domain.PedidoCompraRepository pedidoCompraRepository;
    @Mock private FornecedorRepository fornecedorRepository;
    @Mock private ProdutoRepository produtoRepository;
    @Mock private VariacaoProdutoRepository variacaoProdutoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private MovimentacaoService movimentacaoService;

    @InjectMocks private PedidoCompraServiceImpl service;

    private Fornecedor fornecedor(boolean ativo) {
        return Fornecedor.builder().id(FORNECEDOR_ID).razaoSocial("ACME").cnpj("11222333000181").ativo(ativo).build();
    }

    private Usuario usuario() {
        return Usuario.builder().id(USUARIO_ID).nome("Teste").email("t@t.com").senha("h").build();
    }

    private Produto produto(UUID id, String sku) {
        return Produto.builder().id(id).sku(sku).nome("Produto " + sku).build();
    }

    private ItemRequest itemReq(UUID produtoId, int qtd, String preco) {
        return new ItemRequest(produtoId, null, qtd, new BigDecimal(preco));
    }

    private void stubCriar(boolean fornecedorAtivo) {
        when(fornecedorRepository.findById(FORNECEDOR_ID)).thenReturn(Optional.of(fornecedor(fornecedorAtivo)));
        when(pedidoCompraRepository.save(any(PedidoCompra.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private PedidoCompra pedidoComStatus(StatusPedido status, ItemPedidoCompra... itens) {
        PedidoCompra pedido = PedidoCompra.builder()
                .id(PEDIDO_ID)
                .fornecedor(fornecedor(true))
                .status(status)
                .dataEmissao(LocalDate.now())
                .totalPedido(BigDecimal.ZERO)
                .criadoPor(usuario())
                .itens(new ArrayList<>(List.of(itens)))
                .build();
        pedido.recalcularTotal();
        return pedido;
    }

    private ItemPedidoCompra item(UUID produtoId, int qtd, String preco) {
        ItemPedidoCompra i = ItemPedidoCompra.builder()
                .id(UUID.randomUUID())
                .produto(produto(produtoId, "PRD-0001"))
                .quantidade(qtd)
                .precoUnitario(new BigDecimal(preco))
                .build();
        i.recalcularSubtotal();
        return i;
    }

    @Nested
    @DisplayName("criar")
    class Criar {

        @Test
        @DisplayName("[PC-C1] deve criar pedido em RASCUNHO com total calculado")
        void deveCriarPedidoEmRascunhoComTotal() {
            stubCriar(true);
            when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario()));
            when(produtoRepository.findById(PRODUTO_ID)).thenReturn(Optional.of(produto(PRODUTO_ID, "PRD-0001")));
            when(produtoRepository.findById(PRODUTO_ID_2)).thenReturn(Optional.of(produto(PRODUTO_ID_2, "PRD-0002")));
            var req = new CriarPedidoRequest(FORNECEDOR_ID, LocalDate.now(), null, "obs",
                    List.of(itemReq(PRODUTO_ID, 2, "10.00"), itemReq(PRODUTO_ID_2, 1, "5.50")));

            PedidoCompraResponse resp = service.criar(req, USUARIO_ID);

            assertThat(resp.status()).isEqualTo(StatusPedido.RASCUNHO);
            assertThat(resp.totalPedido()).isEqualByComparingTo("25.50");
            assertThat(resp.itens()).hasSize(2);
        }

        @Test
        @DisplayName("[PC-A1] fornecedor inexistente deve lançar FornecedorNotFoundException")
        void fornecedorInexistenteDeveLancar() {
            when(fornecedorRepository.findById(FORNECEDOR_ID)).thenReturn(Optional.empty());
            var req = new CriarPedidoRequest(FORNECEDOR_ID, LocalDate.now(), null, null,
                    List.of(itemReq(PRODUTO_ID, 1, "1.00")));

            assertThatThrownBy(() -> service.criar(req, USUARIO_ID))
                    .isInstanceOf(FornecedorNotFoundException.class);
            verify(pedidoCompraRepository, never()).save(any());
        }

        @Test
        @DisplayName("[PC-A2] fornecedor inativo deve lançar FornecedorInativoException")
        void fornecedorInativoDeveLancar() {
            when(fornecedorRepository.findById(FORNECEDOR_ID)).thenReturn(Optional.of(fornecedor(false)));
            var req = new CriarPedidoRequest(FORNECEDOR_ID, LocalDate.now(), null, null,
                    List.of(itemReq(PRODUTO_ID, 1, "1.00")));

            assertThatThrownBy(() -> service.criar(req, USUARIO_ID))
                    .isInstanceOf(FornecedorInativoException.class);
            verify(pedidoCompraRepository, never()).save(any());
        }

        @Test
        @DisplayName("[PC-A3] item duplicado (mesmo produto) deve lançar ItemDuplicadoException")
        void itemDuplicadoDeveLancar() {
            when(fornecedorRepository.findById(FORNECEDOR_ID)).thenReturn(Optional.of(fornecedor(true)));
            when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario()));
            when(produtoRepository.findById(PRODUTO_ID)).thenReturn(Optional.of(produto(PRODUTO_ID, "PRD-0001")));
            var req = new CriarPedidoRequest(FORNECEDOR_ID, LocalDate.now(), null, null,
                    List.of(itemReq(PRODUTO_ID, 1, "1.00"), itemReq(PRODUTO_ID, 2, "2.00")));

            assertThatThrownBy(() -> service.criar(req, USUARIO_ID))
                    .isInstanceOf(ItemDuplicadoException.class);
        }
    }

    @Nested
    @DisplayName("itens")
    class Itens {

        @Test
        @DisplayName("[PC-C2] adicionar item em RASCUNHO deve recalcular total")
        void adicionarItemEmRascunhoRecalcula() {
            PedidoCompra pedido = pedidoComStatus(StatusPedido.RASCUNHO, item(PRODUTO_ID, 1, "10.00"));
            when(pedidoCompraRepository.findById(PEDIDO_ID)).thenReturn(Optional.of(pedido));
            when(pedidoCompraRepository.save(any(PedidoCompra.class))).thenAnswer(inv -> inv.getArgument(0));
            when(produtoRepository.findById(PRODUTO_ID_2)).thenReturn(Optional.of(produto(PRODUTO_ID_2, "PRD-0002")));

            PedidoCompraResponse resp = service.adicionarItem(PEDIDO_ID, itemReq(PRODUTO_ID_2, 2, "5.00"));

            assertThat(resp.itens()).hasSize(2);
            assertThat(resp.totalPedido()).isEqualByComparingTo("20.00");
        }

        @Test
        @DisplayName("[PC-C3] adicionar item fora de RASCUNHO deve lançar TransicaoStatusInvalidaException")
        void adicionarItemForaDeRascunhoDeveLancar() {
            PedidoCompra pedido = pedidoComStatus(StatusPedido.PENDENTE, item(PRODUTO_ID, 1, "10.00"));
            when(pedidoCompraRepository.findById(PEDIDO_ID)).thenReturn(Optional.of(pedido));
            when(produtoRepository.findById(PRODUTO_ID_2)).thenReturn(Optional.of(produto(PRODUTO_ID_2, "PRD-0002")));

            assertThatThrownBy(() -> service.adicionarItem(PEDIDO_ID, itemReq(PRODUTO_ID_2, 1, "1.00")))
                    .isInstanceOf(TransicaoStatusInvalidaException.class);
        }

        @Test
        @DisplayName("[PC-C2] remover item em RASCUNHO deve recalcular total")
        void removerItemEmRascunho() {
            ItemPedidoCompra i = item(PRODUTO_ID, 1, "10.00");
            PedidoCompra pedido = pedidoComStatus(StatusPedido.RASCUNHO, i);
            when(pedidoCompraRepository.findById(PEDIDO_ID)).thenReturn(Optional.of(pedido));
            when(pedidoCompraRepository.save(any(PedidoCompra.class))).thenAnswer(inv -> inv.getArgument(0));

            PedidoCompraResponse resp = service.removerItem(PEDIDO_ID, i.getId());

            assertThat(resp.itens()).isEmpty();
            assertThat(resp.totalPedido()).isEqualByComparingTo("0");
        }

        @Test
        @DisplayName("[PC-A4] pedido inexistente deve lançar PedidoCompraNotFoundException")
        void pedidoInexistenteDeveLancar() {
            when(pedidoCompraRepository.findById(PEDIDO_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorId(PEDIDO_ID))
                    .isInstanceOf(PedidoCompraNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("transições de status")
    class Transicoes {

        @Test
        @DisplayName("[PC-C4] enviar RASCUNHO com itens deve ir para PENDENTE")
        void enviarRascunhoComItens() {
            PedidoCompra pedido = pedidoComStatus(StatusPedido.RASCUNHO, item(PRODUTO_ID, 1, "10.00"));
            when(pedidoCompraRepository.findById(PEDIDO_ID)).thenReturn(Optional.of(pedido));
            when(pedidoCompraRepository.save(any(PedidoCompra.class))).thenAnswer(inv -> inv.getArgument(0));

            PedidoCompraResponse resp = service.enviar(PEDIDO_ID);

            assertThat(resp.status()).isEqualTo(StatusPedido.PENDENTE);
        }

        @Test
        @DisplayName("[PC-C4b] enviar RASCUNHO sem itens deve lançar PedidoSemItensException")
        void enviarSemItensDeveLancar() {
            PedidoCompra pedido = pedidoComStatus(StatusPedido.RASCUNHO);
            when(pedidoCompraRepository.findById(PEDIDO_ID)).thenReturn(Optional.of(pedido));

            assertThatThrownBy(() -> service.enviar(PEDIDO_ID))
                    .isInstanceOf(PedidoSemItensException.class);
        }

        @Test
        @DisplayName("[PC-C5] aprovar PENDENTE deve ir para APROVADO")
        void aprovarPendente() {
            PedidoCompra pedido = pedidoComStatus(StatusPedido.PENDENTE, item(PRODUTO_ID, 1, "10.00"));
            when(pedidoCompraRepository.findById(PEDIDO_ID)).thenReturn(Optional.of(pedido));
            when(pedidoCompraRepository.save(any(PedidoCompra.class))).thenAnswer(inv -> inv.getArgument(0));

            PedidoCompraResponse resp = service.aprovar(PEDIDO_ID);

            assertThat(resp.status()).isEqualTo(StatusPedido.APROVADO);
        }

        @Test
        @DisplayName("[PC-C5b] aprovar fora de PENDENTE deve lançar TransicaoStatusInvalidaException")
        void aprovarForaDePendenteDeveLancar() {
            PedidoCompra pedido = pedidoComStatus(StatusPedido.RASCUNHO, item(PRODUTO_ID, 1, "10.00"));
            when(pedidoCompraRepository.findById(PEDIDO_ID)).thenReturn(Optional.of(pedido));

            assertThatThrownBy(() -> service.aprovar(PEDIDO_ID))
                    .isInstanceOf(TransicaoStatusInvalidaException.class);
        }

        @Test
        @DisplayName("[PC-C7] cancelar PENDENTE deve ir para CANCELADO")
        void cancelarPendente() {
            PedidoCompra pedido = pedidoComStatus(StatusPedido.PENDENTE, item(PRODUTO_ID, 1, "10.00"));
            when(pedidoCompraRepository.findById(PEDIDO_ID)).thenReturn(Optional.of(pedido));
            when(pedidoCompraRepository.save(any(PedidoCompra.class))).thenAnswer(inv -> inv.getArgument(0));

            PedidoCompraResponse resp = service.cancelar(PEDIDO_ID);

            assertThat(resp.status()).isEqualTo(StatusPedido.CANCELADO);
        }

        @Test
        @DisplayName("[PC-C8] cancelar APROVADO deve lançar TransicaoStatusInvalidaException")
        void cancelarAprovadoDeveLancar() {
            PedidoCompra pedido = pedidoComStatus(StatusPedido.APROVADO, item(PRODUTO_ID, 1, "10.00"));
            when(pedidoCompraRepository.findById(PEDIDO_ID)).thenReturn(Optional.of(pedido));

            assertThatThrownBy(() -> service.cancelar(PEDIDO_ID))
                    .isInstanceOf(TransicaoStatusInvalidaException.class);
        }
    }

    @Nested
    @DisplayName("receber")
    class Receber {

        @Test
        @DisplayName("[PC-C6] receber APROVADO deve ir para RECEBIDO e gerar ENTRADA por item")
        void receberAprovadoGeraEntradaPorItem() {
            PedidoCompra pedido = pedidoComStatus(StatusPedido.APROVADO,
                    item(PRODUTO_ID, 2, "10.00"), item(PRODUTO_ID_2, 3, "5.00"));
            when(pedidoCompraRepository.findById(PEDIDO_ID)).thenReturn(Optional.of(pedido));
            when(pedidoCompraRepository.save(any(PedidoCompra.class))).thenAnswer(inv -> inv.getArgument(0));

            PedidoCompraResponse resp = service.receber(PEDIDO_ID, USUARIO_ID);

            assertThat(resp.status()).isEqualTo(StatusPedido.RECEBIDO);
            verify(movimentacaoService, times(2))
                    .registrarEntradaPorPedido(any(EntradaPedidoComando.class), eq(USUARIO_ID));
        }

        @Test
        @DisplayName("[PC-C6b] receber fora de APROVADO deve lançar e não gerar movimentação")
        void receberForaDeAprovadoDeveLancar() {
            PedidoCompra pedido = pedidoComStatus(StatusPedido.PENDENTE, item(PRODUTO_ID, 1, "10.00"));
            when(pedidoCompraRepository.findById(PEDIDO_ID)).thenReturn(Optional.of(pedido));

            assertThatThrownBy(() -> service.receber(PEDIDO_ID, USUARIO_ID))
                    .isInstanceOf(TransicaoStatusInvalidaException.class);
            verify(movimentacaoService, never()).registrarEntradaPorPedido(any(), any());
        }
    }
}
