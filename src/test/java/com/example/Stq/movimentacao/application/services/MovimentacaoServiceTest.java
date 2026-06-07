package com.example.Stq.movimentacao.application.services;

import com.example.Stq.autenticacao.domain.Usuario;
import com.example.Stq.autenticacao.domain.UsuarioRepository;
import com.example.Stq.movimentacao.application.dto.AjusteRequest;
import com.example.Stq.movimentacao.application.dto.EntradaRequest;
import com.example.Stq.movimentacao.application.dto.SaidaRequest;
import com.example.Stq.movimentacao.application.dto.TransferenciaRequest;
import com.example.Stq.movimentacao.domain.Estoque;
import com.example.Stq.movimentacao.domain.EstoqueRepository;
import com.example.Stq.movimentacao.domain.Movimentacao;
import com.example.Stq.movimentacao.domain.MovimentacaoRepository;
import com.example.Stq.movimentacao.domain.OrigemMovimentacao;
import com.example.Stq.movimentacao.domain.TipoMovimentacao;
import com.example.Stq.movimentacao.domain.exception.EstoqueNotFoundException;
import com.example.Stq.movimentacao.domain.exception.LocalizacaoTransferenciaInvalidaException;
import com.example.Stq.movimentacao.domain.exception.QuantidadeInvalidaException;
import com.example.Stq.movimentacao.domain.exception.SaldoInsuficienteException;
import com.example.Stq.produto.domain.Produto;
import com.example.Stq.produto.domain.ProdutoRepository;
import com.example.Stq.produto.domain.VariacaoProdutoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovimentacaoServiceImpl")
class MovimentacaoServiceTest {

    private static final UUID PRODUTO_ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final UUID USUARIO_ID = UUID.fromString("00000000-0000-0000-0000-0000000000b2");

    @Mock
    private EstoqueRepository estoqueRepository;
    @Mock
    private MovimentacaoRepository movimentacaoRepository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private VariacaoProdutoRepository variacaoProdutoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private MovimentacaoServiceImpl service;

    private Produto produto() {
        return Produto.builder().id(PRODUTO_ID).sku("PRD-000001").estoqueMinimo(5).build();
    }

    private Usuario usuario() {
        return Usuario.builder().id(USUARIO_ID).nome("Teste").email("t@t.com").senha("h").build();
    }

    private Estoque estoqueComSaldo(int saldo, String localizacao) {
        return Estoque.builder()
                .id(UUID.randomUUID())
                .produto(produto())
                .localizacao(localizacao)
                .saldoAtual(saldo)
                .build();
    }

    private void stubProdutoEUsuario() {
        when(produtoRepository.findById(PRODUTO_ID)).thenReturn(Optional.of(produto()));
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario()));
        when(movimentacaoRepository.save(any(Movimentacao.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    private ArgumentCaptor<Movimentacao> capturarMovimentacao() {
        ArgumentCaptor<Movimentacao> captor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(movimentacaoRepository).save(captor.capture());
        return captor;
    }

    @Nested
    @DisplayName("registrarEntrada")
    class Entrada {

        @Test
        @DisplayName("[MOV-C1] entrada manual deve incrementar saldo e criar estoque inexistente")
        void entradaDeveIncrementarSaldoECriarEstoque() {
            stubProdutoEUsuario();
            when(estoqueRepository.buscarCombinacao(PRODUTO_ID, null, "A1"))
                    .thenReturn(Optional.empty());
            var req = new EntradaRequest(PRODUTO_ID, null, "A1", 7, "compra");

            var resposta = service.registrarEntrada(req, USUARIO_ID);

            ArgumentCaptor<Estoque> estoqueCaptor = ArgumentCaptor.forClass(Estoque.class);
            verify(estoqueRepository).save(estoqueCaptor.capture());
            assertThat(estoqueCaptor.getValue().getSaldoAtual()).isEqualTo(7);

            Movimentacao mov = capturarMovimentacao().getValue();
            assertThat(mov.getTipo()).isEqualTo(TipoMovimentacao.ENTRADA);
            assertThat(mov.getOrigem()).isEqualTo(OrigemMovimentacao.MANUAL);
            assertThat(mov.getQuantidade()).isEqualTo(7);
            assertThat(mov.getSaldoAntes()).isZero();
            assertThat(mov.getSaldoDepois()).isEqualTo(7);
            assertThat(resposta.saldoDepois()).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("registrarSaida")
    class Saida {

        @Test
        @DisplayName("[MOV-C2] saída manual deve decrementar saldo")
        void saidaDeveDecrementarSaldo() {
            stubProdutoEUsuario();
            when(estoqueRepository.buscarCombinacao(PRODUTO_ID, null, "A1"))
                    .thenReturn(Optional.of(estoqueComSaldo(10, "A1")));
            var req = new SaidaRequest(PRODUTO_ID, null, "A1", 4, null);

            service.registrarSaida(req, USUARIO_ID);

            Movimentacao mov = capturarMovimentacao().getValue();
            assertThat(mov.getTipo()).isEqualTo(TipoMovimentacao.SAIDA);
            assertThat(mov.getSaldoAntes()).isEqualTo(10);
            assertThat(mov.getSaldoDepois()).isEqualTo(6);
        }

        @Test
        @DisplayName("[MOV-C3b] saída sem registro de estoque deve lançar EstoqueNotFoundException")
        void saidaSemEstoqueDeveLancarExcecao() {
            when(produtoRepository.findById(PRODUTO_ID)).thenReturn(Optional.of(produto()));
            when(estoqueRepository.buscarCombinacao(PRODUTO_ID, null, "A1"))
                    .thenReturn(Optional.empty());
            var req = new SaidaRequest(PRODUTO_ID, null, "A1", 1, null);

            assertThatThrownBy(() -> service.registrarSaida(req, USUARIO_ID))
                    .isInstanceOf(EstoqueNotFoundException.class);

            verify(movimentacaoRepository, never()).save(any());
            verify(estoqueRepository, never()).save(any());
        }

        @Test
        @DisplayName("[MOV-C3] saída com saldo insuficiente deve lançar SaldoInsuficienteException")
        void saidaInsuficienteDeveLancarExcecao() {
            when(produtoRepository.findById(PRODUTO_ID)).thenReturn(Optional.of(produto()));
            when(estoqueRepository.buscarCombinacao(PRODUTO_ID, null, "A1"))
                    .thenReturn(Optional.of(estoqueComSaldo(2, "A1")));
            var req = new SaidaRequest(PRODUTO_ID, null, "A1", 5, null);

            assertThatThrownBy(() -> service.registrarSaida(req, USUARIO_ID))
                    .isInstanceOf(SaldoInsuficienteException.class);

            verify(movimentacaoRepository, never()).save(any());
            verify(estoqueRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("registrarTransferencia")
    class Transferencia {

        @Test
        @DisplayName("[MOV-C4] transferência deve decrementar origem e incrementar destino com uma única Movimentacao")
        void transferenciaDeveMoverSaldo() {
            stubProdutoEUsuario();
            when(estoqueRepository.buscarCombinacao(PRODUTO_ID, null, "A1"))
                    .thenReturn(Optional.of(estoqueComSaldo(10, "A1")));
            when(estoqueRepository.buscarCombinacao(PRODUTO_ID, null, "B2"))
                    .thenReturn(Optional.empty());
            var req = new TransferenciaRequest(PRODUTO_ID, null, "A1", "B2", 3, null);

            service.registrarTransferencia(req, USUARIO_ID);

            ArgumentCaptor<Estoque> estoqueCaptor = ArgumentCaptor.forClass(Estoque.class);
            verify(estoqueRepository, org.mockito.Mockito.times(2)).save(estoqueCaptor.capture());
            assertThat(estoqueCaptor.getAllValues().get(0).getSaldoAtual()).isEqualTo(7);
            assertThat(estoqueCaptor.getAllValues().get(1).getSaldoAtual()).isEqualTo(3);

            Movimentacao mov = capturarMovimentacao().getValue();
            assertThat(mov.getTipo()).isEqualTo(TipoMovimentacao.TRANSFERENCIA);
            assertThat(mov.getSaldoAntes()).isEqualTo(10);
            assertThat(mov.getSaldoDepois()).isEqualTo(7);
        }

        @Test
        @DisplayName("[MOV-C4c] transferência sem estoque na origem deve lançar EstoqueNotFoundException")
        void transferenciaSemEstoqueNaOrigemDeveLancarExcecao() {
            when(produtoRepository.findById(PRODUTO_ID)).thenReturn(Optional.of(produto()));
            when(estoqueRepository.buscarCombinacao(PRODUTO_ID, null, "A1"))
                    .thenReturn(Optional.empty());
            var req = new TransferenciaRequest(PRODUTO_ID, null, "A1", "B2", 3, null);

            assertThatThrownBy(() -> service.registrarTransferencia(req, USUARIO_ID))
                    .isInstanceOf(EstoqueNotFoundException.class);

            verify(movimentacaoRepository, never()).save(any());
        }

        @Test
        @DisplayName("[MOV-C4b] transferência com localização igual deve lançar LocalizacaoTransferenciaInvalidaException")
        void transferenciaComLocalizacaoIgualDeveLancarExcecao() {
            var req = new TransferenciaRequest(PRODUTO_ID, null, "A1", "A1", 3, null);

            assertThatThrownBy(() -> service.registrarTransferencia(req, USUARIO_ID))
                    .isInstanceOf(LocalizacaoTransferenciaInvalidaException.class);

            verify(movimentacaoRepository, never()).save(any());
            verify(estoqueRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("registrarAjuste")
    class Ajuste {

        @Test
        @DisplayName("[MOV-C5] ajuste ADMIN com delta negativo deve permitir saldo negativo e gravar quantidade absoluta")
        void ajusteComDeltaNegativoDevePermitirSaldoNegativo() {
            stubProdutoEUsuario();
            when(estoqueRepository.buscarCombinacao(PRODUTO_ID, null, "A1"))
                    .thenReturn(Optional.of(estoqueComSaldo(3, "A1")));
            var req = new AjusteRequest(PRODUTO_ID, null, "A1", -5, "contagem");

            service.registrarAjuste(req, USUARIO_ID);

            Movimentacao mov = capturarMovimentacao().getValue();
            assertThat(mov.getTipo()).isEqualTo(TipoMovimentacao.AJUSTE);
            assertThat(mov.getOrigem()).isEqualTo(OrigemMovimentacao.AJUSTE_INVENTARIO);
            assertThat(mov.getQuantidade()).isEqualTo(5);
            assertThat(mov.getSaldoAntes()).isEqualTo(3);
            assertThat(mov.getSaldoDepois()).isEqualTo(-2);
        }

        @Test
        @DisplayName("[MOV-C5b] ajuste com delta zero deve lançar QuantidadeInvalidaException")
        void ajusteComDeltaZeroDeveLancarExcecao() {
            var req = new AjusteRequest(PRODUTO_ID, null, "A1", 0, null);

            assertThatThrownBy(() -> service.registrarAjuste(req, USUARIO_ID))
                    .isInstanceOf(QuantidadeInvalidaException.class);

            verify(movimentacaoRepository, never()).save(any());
            verify(estoqueRepository, never()).save(any());
        }
    }
}
