package com.example.Stq.frontend.web;

import com.example.Stq.movimentacao.domain.TipoMovimentacao;
import com.example.Stq.pedidocompra.domain.StatusPedido;
import com.example.Stq.relatorio.application.services.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/web/relatorios")
@RequiredArgsConstructor
public class RelatorioWebController {

    private final RelatorioService relatorioService;

    @GetMapping
    public String index() {
        return "relatorio/index";
    }

    @GetMapping("/estoque")
    public String estoqueAtual(Model model) {
        model.addAttribute("itens", relatorioService.estoqueAtual());
        return "relatorio/estoque";
    }

    @GetMapping("/alertas")
    public String alertas(Model model) {
        model.addAttribute("itens", relatorioService.alertas());
        return "relatorio/alertas";
    }

    @GetMapping("/movimentacoes")
    public String movimentacoes(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(required = false) String tipo,
            Model model) {
        TipoMovimentacao tipoEnum = (tipo != null && !tipo.isBlank())
                ? TipoMovimentacao.valueOf(tipo)
                : null;
        model.addAttribute("relatorio", relatorioService.movimentacoes(de, ate, tipoEnum));
        model.addAttribute("filtroDe", de);
        model.addAttribute("filtroAte", ate);
        model.addAttribute("filtroTipo", tipo);
        model.addAttribute("tipos", TipoMovimentacao.values());
        return "relatorio/movimentacoes";
    }

    @GetMapping("/pedidos")
    public String pedidos(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(required = false) String status,
            Model model) {
        StatusPedido statusEnum = (status != null && !status.isBlank())
                ? StatusPedido.valueOf(status)
                : null;
        model.addAttribute("relatorio", relatorioService.pedidos(de, ate, statusEnum));
        model.addAttribute("filtroDe", de);
        model.addAttribute("filtroAte", ate);
        model.addAttribute("filtroStatus", status);
        model.addAttribute("statusPedido", StatusPedido.values());
        return "relatorio/pedidos";
    }
}
