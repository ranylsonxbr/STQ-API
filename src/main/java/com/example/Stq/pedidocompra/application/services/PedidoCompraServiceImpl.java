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
import com.example.Stq.pedidocompra.domain.PedidoCompraFiltro;
import com.example.Stq.pedidocompra.domain.PedidoCompraRepository;
import com.example.Stq.pedidocompra.domain.exception.FornecedorInativoException;
import com.example.Stq.pedidocompra.domain.exception.PedidoCompraNotFoundException;
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
public class PedidoCompraServiceImpl implements PedidoCompraService {

    private final PedidoCompraRepository pedidoCompraRepository;
    private final FornecedorRepository fornecedorRepository;
    private final ProdutoRepository produtoRepository;
    private final VariacaoProdutoRepository variacaoProdutoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimentacaoService movimentacaoService;

    @Override
    @Transactional
    public PedidoCompraResponse criar(CriarPedidoRequest request, UUID usuarioId) {
        Fornecedor fornecedor = carregarFornecedor(request.fornecedorId());
        if (!fornecedor.isAtivo()) {
            throw new FornecedorInativoException();
        }
        Usuario criadoPor = carregarUsuario(usuarioId);

        PedidoCompra pedido = PedidoCompra.builder()
                .fornecedor(fornecedor)
                .dataEmissao(request.dataEmissao())
                .dataPrevisaoEntrega(request.dataPrevisaoEntrega())
                .observacao(request.observacao())
                .criadoPor(criadoPor)
                .build();

        for (ItemRequest itemReq : request.itens()) {
            pedido.adicionarItem(montarItem(itemReq));
        }

        return PedidoCompraResponse.de(pedidoCompraRepository.save(pedido));
    }

    @Override
    public PedidoCompraResponse buscarPorId(UUID id) {
        return PedidoCompraResponse.de(carregarPedido(id));
    }

    @Override
    public Page<PedidoCompraResponse> listar(PedidoCompraFiltro filtro, Pageable pageable) {
        return pedidoCompraRepository.findAll(filtro, pageable).map(PedidoCompraResponse::de);
    }

    @Override
    @Transactional
    public PedidoCompraResponse adicionarItem(UUID pedidoId, ItemRequest request) {
        PedidoCompra pedido = carregarPedido(pedidoId);
        pedido.adicionarItem(montarItem(request));
        return PedidoCompraResponse.de(pedidoCompraRepository.save(pedido));
    }

    @Override
    @Transactional
    public PedidoCompraResponse removerItem(UUID pedidoId, UUID itemId) {
        PedidoCompra pedido = carregarPedido(pedidoId);
        pedido.removerItem(itemId);
        return PedidoCompraResponse.de(pedidoCompraRepository.save(pedido));
    }

    @Override
    @Transactional
    public PedidoCompraResponse enviar(UUID pedidoId) {
        PedidoCompra pedido = carregarPedido(pedidoId);
        pedido.enviar();
        return PedidoCompraResponse.de(pedidoCompraRepository.save(pedido));
    }

    @Override
    @Transactional
    public PedidoCompraResponse aprovar(UUID pedidoId) {
        PedidoCompra pedido = carregarPedido(pedidoId);
        pedido.aprovar();
        return PedidoCompraResponse.de(pedidoCompraRepository.save(pedido));
    }

    @Override
    @Transactional
    public PedidoCompraResponse receber(UUID pedidoId, UUID usuarioId) {
        PedidoCompra pedido = carregarPedido(pedidoId);
        pedido.receber();
        for (ItemPedidoCompra item : pedido.getItens()) {
            UUID variacaoId = item.getVariacao() != null ? item.getVariacao().getId() : null;
            var comando = new EntradaPedidoComando(
                    item.getProduto().getId(),
                    variacaoId,
                    item.getQuantidade(),
                    "Recebimento do pedido " + pedido.getId());
            movimentacaoService.registrarEntradaPorPedido(comando, usuarioId);
        }
        return PedidoCompraResponse.de(pedidoCompraRepository.save(pedido));
    }

    @Override
    @Transactional
    public PedidoCompraResponse cancelar(UUID pedidoId) {
        PedidoCompra pedido = carregarPedido(pedidoId);
        pedido.cancelar();
        return PedidoCompraResponse.de(pedidoCompraRepository.save(pedido));
    }

    private ItemPedidoCompra montarItem(ItemRequest req) {
        Produto produto = carregarProduto(req.produtoId());
        VariacaoProduto variacao = carregarVariacaoOpcional(req.variacaoId(), produto.getId());
        return ItemPedidoCompra.builder()
                .produto(produto)
                .variacao(variacao)
                .quantidade(req.quantidade())
                .precoUnitario(req.precoUnitario())
                .build();
    }

    private PedidoCompra carregarPedido(UUID id) {
        return pedidoCompraRepository.findById(id)
                .orElseThrow(() -> new PedidoCompraNotFoundException(id));
    }

    private Fornecedor carregarFornecedor(UUID id) {
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new FornecedorNotFoundException(id));
    }

    private Usuario carregarUsuario(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado: " + id));
    }

    private Produto carregarProduto(UUID id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoNotFoundException(id));
    }

    private VariacaoProduto carregarVariacaoOpcional(UUID variacaoId, UUID produtoId) {
        if (variacaoId == null) {
            return null;
        }
        return variacaoProdutoRepository.findByIdAndProdutoId(variacaoId, produtoId)
                .orElseThrow(() -> new VariacaoNotFoundException(variacaoId));
    }
}
