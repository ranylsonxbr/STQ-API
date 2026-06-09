package com.example.Stq.frontend.web;

import com.example.Stq.fornecedor.application.services.FornecedorService;
import com.example.Stq.fornecedor.domain.FornecedorFiltro;
import com.example.Stq.movimentacao.application.services.MovimentacaoService;
import com.example.Stq.movimentacao.domain.MovimentacaoFiltro;
import com.example.Stq.pedidocompra.application.services.PedidoCompraService;
import com.example.Stq.pedidocompra.domain.PedidoCompraFiltro;
import com.example.Stq.pedidocompra.domain.StatusPedido;
import com.example.Stq.produto.application.services.ProdutoService;
import com.example.Stq.relatorio.application.services.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardWebController {

    private final RelatorioService relatorioService;
    private final ProdutoService produtoService;
    private final FornecedorService fornecedorService;
    private final PedidoCompraService pedidoCompraService;
    private final MovimentacaoService movimentacaoService;

    @GetMapping("/web/dashboard")
    public String dashboard(Model model) {
        var alertas = relatorioService.alertas();
        model.addAttribute("totalAlertas", alertas.size());
        model.addAttribute("alertas", alertas.size() > 10 ? alertas.subList(0, 10) : alertas);

        long totalProdutosAtivos = produtoService
                .listar(null, true, null, PageRequest.of(0, 1))
                .getTotalElements();
        model.addAttribute("totalProdutosAtivos", totalProdutosAtivos);

        long totalFornecedoresAtivos = fornecedorService
                .listar(new FornecedorFiltro(null, null, true), PageRequest.of(0, 1))
                .getTotalElements();
        model.addAttribute("totalFornecedoresAtivos", totalFornecedoresAtivos);

        var pedidosPendentes = pedidoCompraService
                .listar(new PedidoCompraFiltro(null, StatusPedido.PENDENTE, null, null), PageRequest.of(0, 5));
        var pedidosAprovados = pedidoCompraService
                .listar(new PedidoCompraFiltro(null, StatusPedido.APROVADO, null, null), PageRequest.of(0, 5));

        model.addAttribute("totalPedidosAbertos",
                pedidosPendentes.getTotalElements() + pedidosAprovados.getTotalElements());
        model.addAttribute("pedidosPendentes", pedidosPendentes.getContent());
        model.addAttribute("pedidosAprovados", pedidosAprovados.getContent());

        var ultimasMovimentacoes = movimentacaoService.listar(
                new MovimentacaoFiltro(null, null, null, null),
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "realizadoEm")));
        model.addAttribute("ultimasMovimentacoes", ultimasMovimentacoes.getContent());

        return "dashboard/index";
    }
}
