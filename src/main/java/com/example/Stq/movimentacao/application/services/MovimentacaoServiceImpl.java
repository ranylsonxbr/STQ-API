package com.example.Stq.movimentacao.application.services;

import com.example.Stq.autenticacao.domain.Usuario;
import com.example.Stq.autenticacao.domain.UsuarioRepository;
import com.example.Stq.movimentacao.application.dto.AjusteRequest;
import com.example.Stq.movimentacao.application.dto.EntradaPedidoComando;
import com.example.Stq.movimentacao.application.dto.EntradaRequest;
import com.example.Stq.movimentacao.application.dto.MovimentacaoResponse;
import com.example.Stq.movimentacao.application.dto.SaidaRequest;
import com.example.Stq.movimentacao.application.dto.TransferenciaRequest;
import com.example.Stq.movimentacao.domain.Estoque;
import com.example.Stq.movimentacao.domain.EstoqueRepository;
import com.example.Stq.movimentacao.domain.Movimentacao;
import com.example.Stq.movimentacao.domain.MovimentacaoFiltro;
import com.example.Stq.movimentacao.domain.MovimentacaoRepository;
import com.example.Stq.movimentacao.domain.OrigemMovimentacao;
import com.example.Stq.movimentacao.domain.TipoMovimentacao;
import com.example.Stq.movimentacao.domain.exception.EstoqueNotFoundException;
import com.example.Stq.movimentacao.domain.exception.LocalizacaoTransferenciaInvalidaException;
import com.example.Stq.movimentacao.domain.exception.QuantidadeInvalidaException;
import com.example.Stq.movimentacao.domain.exception.SaldoInsuficienteException;
import com.example.Stq.produto.domain.Produto;
import com.example.Stq.produto.domain.ProdutoRepository;
import com.example.Stq.produto.domain.VariacaoProduto;
import com.example.Stq.produto.domain.VariacaoProdutoRepository;
import com.example.Stq.produto.domain.exception.ProdutoNotFoundException;
import com.example.Stq.produto.domain.exception.VariacaoNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MovimentacaoServiceImpl implements MovimentacaoService {

    private static final String LOCALIZACAO_PADRAO = "PADRAO";

    private final EstoqueRepository estoqueRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final ProdutoRepository produtoRepository;
    private final VariacaoProdutoRepository variacaoProdutoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public MovimentacaoResponse registrarEntrada(EntradaRequest req, UUID usuarioId) {
        Produto produto = carregarProduto(req.produtoId());
        VariacaoProduto variacao = carregarVariacaoOpcional(req.variacaoId(), produto.getId());
        Estoque estoque = obterOuCriarEstoque(produto, variacao, req.localizacao());

        int antes = estoque.getSaldoAtual();
        int depois = antes + req.quantidade();
        estoque.setSaldoAtual(depois);
        estoqueRepository.save(estoque);

        return MovimentacaoResponse.de(gravarMovimentacao(
                produto, variacao, TipoMovimentacao.ENTRADA, OrigemMovimentacao.MANUAL,
                req.quantidade(), antes, depois, req.observacao(), usuarioId));
    }

    @Override
    @Transactional
    public MovimentacaoResponse registrarEntradaPorPedido(EntradaPedidoComando cmd, UUID usuarioId) {
        Produto produto = carregarProduto(cmd.produtoId());
        VariacaoProduto variacao = carregarVariacaoOpcional(cmd.variacaoId(), produto.getId());
        Estoque estoque = obterOuCriarEstoque(produto, variacao, LOCALIZACAO_PADRAO);

        int antes = estoque.getSaldoAtual();
        int depois = antes + cmd.quantidade();
        estoque.setSaldoAtual(depois);
        estoqueRepository.save(estoque);

        return MovimentacaoResponse.de(gravarMovimentacao(
                produto, variacao, TipoMovimentacao.ENTRADA, OrigemMovimentacao.PEDIDO_COMPRA,
                cmd.quantidade(), antes, depois, cmd.observacao(), usuarioId));
    }

    @Override
    @Transactional
    public MovimentacaoResponse registrarSaida(SaidaRequest req, UUID usuarioId) {
        Produto produto = carregarProduto(req.produtoId());
        VariacaoProduto variacao = carregarVariacaoOpcional(req.variacaoId(), produto.getId());
        Estoque estoque = estoqueRepository
                .buscarCombinacao(produto.getId(), idOuNull(variacao), req.localizacao())
                .orElseThrow(() -> new EstoqueNotFoundException(produto.getId()));

        int antes = estoque.getSaldoAtual();
        if (antes < req.quantidade()) {
            throw new SaldoInsuficienteException(antes, req.quantidade());
        }
        int depois = antes - req.quantidade();
        estoque.setSaldoAtual(depois);
        estoqueRepository.save(estoque);

        return MovimentacaoResponse.de(gravarMovimentacao(
                produto, variacao, TipoMovimentacao.SAIDA, OrigemMovimentacao.MANUAL,
                req.quantidade(), antes, depois, req.observacao(), usuarioId));
    }

    @Override
    @Transactional
    public MovimentacaoResponse registrarTransferencia(TransferenciaRequest req, UUID usuarioId) {
        if (req.localizacaoOrigem().equals(req.localizacaoDestino())) {
            throw new LocalizacaoTransferenciaInvalidaException();
        }
        Produto produto = carregarProduto(req.produtoId());
        VariacaoProduto variacao = carregarVariacaoOpcional(req.variacaoId(), produto.getId());

        Estoque origem = estoqueRepository
                .buscarCombinacao(produto.getId(), idOuNull(variacao), req.localizacaoOrigem())
                .orElseThrow(() -> new EstoqueNotFoundException(produto.getId()));

        int antes = origem.getSaldoAtual();
        if (antes < req.quantidade()) {
            throw new SaldoInsuficienteException(antes, req.quantidade());
        }
        int depois = antes - req.quantidade();
        origem.setSaldoAtual(depois);
        estoqueRepository.save(origem);

        Estoque destino = obterOuCriarEstoque(produto, variacao, req.localizacaoDestino());
        destino.setSaldoAtual(destino.getSaldoAtual() + req.quantidade());
        estoqueRepository.save(destino);

        return MovimentacaoResponse.de(gravarMovimentacao(
                produto, variacao, TipoMovimentacao.TRANSFERENCIA, OrigemMovimentacao.MANUAL,
                req.quantidade(), antes, depois, req.observacao(), usuarioId));
    }

    @Override
    @Transactional
    public MovimentacaoResponse registrarAjuste(AjusteRequest req, UUID usuarioId) {
        if (req.delta() == 0) {
            throw new QuantidadeInvalidaException("O delta do ajuste não pode ser zero.");
        }
        Produto produto = carregarProduto(req.produtoId());
        VariacaoProduto variacao = carregarVariacaoOpcional(req.variacaoId(), produto.getId());
        Estoque estoque = obterOuCriarEstoque(produto, variacao, req.localizacao());

        int antes = estoque.getSaldoAtual();
        int depois = antes + req.delta();
        estoque.setSaldoAtual(depois);
        estoqueRepository.save(estoque);

        return MovimentacaoResponse.de(gravarMovimentacao(
                produto, variacao, TipoMovimentacao.AJUSTE, OrigemMovimentacao.AJUSTE_INVENTARIO,
                Math.abs(req.delta()), antes, depois, req.observacao(), usuarioId));
    }

    @Override
    public Page<MovimentacaoResponse> listar(MovimentacaoFiltro filtro, Pageable pageable) {
        return movimentacaoRepository.findAll(filtro, pageable).map(MovimentacaoResponse::de);
    }

    private Produto carregarProduto(UUID produtoId) {
        return produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ProdutoNotFoundException(produtoId));
    }

    private VariacaoProduto carregarVariacaoOpcional(UUID variacaoId, UUID produtoId) {
        if (variacaoId == null) {
            return null;
        }
        return variacaoProdutoRepository.findByIdAndProdutoId(variacaoId, produtoId)
                .orElseThrow(() -> new VariacaoNotFoundException(variacaoId));
    }

    private Estoque obterOuCriarEstoque(Produto produto, VariacaoProduto variacao, String localizacao) {
        return estoqueRepository
                .buscarCombinacao(produto.getId(), idOuNull(variacao), localizacao)
                .orElseGet(() -> Estoque.builder()
                        .produto(produto)
                        .variacao(variacao)
                        .localizacao(localizacao)
                        .saldoAtual(0)
                        .build());
    }

    private Movimentacao gravarMovimentacao(Produto produto, VariacaoProduto variacao,
                                            TipoMovimentacao tipo, OrigemMovimentacao origem,
                                            int quantidade, int saldoAntes, int saldoDepois,
                                            String observacao, UUID usuarioId) {
        Usuario realizadoPor = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado: " + usuarioId));
        Movimentacao movimentacao = Movimentacao.builder()
                .produto(produto)
                .variacao(variacao)
                .tipo(tipo)
                .origem(origem)
                .quantidade(quantidade)
                .saldoAntes(saldoAntes)
                .saldoDepois(saldoDepois)
                .observacao(observacao)
                .realizadoPor(realizadoPor)
                .build();
        return movimentacaoRepository.save(movimentacao);
    }

    private UUID idOuNull(VariacaoProduto variacao) {
        return variacao != null ? variacao.getId() : null;
    }
}
