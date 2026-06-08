package com.example.Stq.frontend.web;

import com.example.Stq.fornecedor.application.services.FornecedorService;
import com.example.Stq.fornecedor.domain.FornecedorFiltro;
import com.example.Stq.pedidocompra.application.services.PedidoCompraService;
import com.example.Stq.pedidocompra.domain.PedidoCompraFiltro;
import com.example.Stq.pedidocompra.domain.StatusPedido;
import com.example.Stq.produto.application.services.ProdutoService;
import com.example.Stq.relatorio.application.services.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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

    @GetMapping("/web/dashboard")
    public String dashboard(Model model) {
        var alertas = relatorioService.alertas();
        model.addAttribute("totalAlertas", alertas.size());

        long totalProdutosAtivos = produtoService
                .listar(null, true, null, PageRequest.of(0, 1))
                .getTotalElements();
        model.addAttribute("totalProdutosAtivos", totalProdutosAtivos);

        long totalFornecedoresAtivos = fornecedorService
                .listar(new FornecedorFiltro(null, null, true), PageRequest.of(0, 1))
                .getTotalElements();
        model.addAttribute("totalFornecedoresAtivos", totalFornecedoresAtivos);

        long totalPedidosPendentes = pedidoCompraService
                .listar(new PedidoCompraFiltro(null, StatusPedido.PENDENTE, null, null), PageRequest.of(0, 1))
                .getTotalElements();
        long totalPedidosAprovados = pedidoCompraService
                .listar(new PedidoCompraFiltro(null, StatusPedido.APROVADO, null, null), PageRequest.of(0, 1))
                .getTotalElements();
        model.addAttribute("totalPedidosAbertos", totalPedidosPendentes + totalPedidosAprovados);

        return "dashboard/index";
    }
}
