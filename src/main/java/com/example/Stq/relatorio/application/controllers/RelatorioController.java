package com.example.Stq.relatorio.application.controllers;

import com.example.Stq.movimentacao.domain.TipoMovimentacao;
import com.example.Stq.pedidocompra.domain.StatusPedido;
import com.example.Stq.relatorio.application.dto.EstoqueAtualItem;
import com.example.Stq.relatorio.application.dto.RelatorioPedidos;
import com.example.Stq.relatorio.application.dto.RelatorioMovimentacoes;
import com.example.Stq.relatorio.application.services.RelatorioService;
import com.example.Stq.relatorio.infra.CsvHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
@PreAuthorize("hasAnyRole('VISUALIZADOR','OPERADOR','ADMIN')")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/estoque-atual")
    public ResponseEntity<?> estoqueAtual(HttpServletRequest request) {
        List<EstoqueAtualItem> dados = relatorioService.estoqueAtual();
        if (aceitaCsv(request)) {
            String csv = CsvHelper.toCsv(
                    new String[]{"SKU", "Produto", "Unidade", "Saldo Atual", "Estoque Mínimo", "Status"},
                    dados.stream().map(i -> new String[]{
                            i.sku(), i.nomeProduto(), i.unidadeMedida(),
                            String.valueOf(i.saldoAtual()), String.valueOf(i.estoqueMinimo()),
                            i.status().name()
                    }).toList()
            );
            return csvResponse(csv, "relatorio-estoque-atual.csv");
        }
        return ResponseEntity.ok(dados);
    }

    @GetMapping("/alertas")
    public ResponseEntity<?> alertas(HttpServletRequest request) {
        List<EstoqueAtualItem> dados = relatorioService.alertas();
        if (aceitaCsv(request)) {
            String csv = CsvHelper.toCsv(
                    new String[]{"SKU", "Produto", "Unidade", "Saldo Atual", "Estoque Mínimo", "Status"},
                    dados.stream().map(i -> new String[]{
                            i.sku(), i.nomeProduto(), i.unidadeMedida(),
                            String.valueOf(i.saldoAtual()), String.valueOf(i.estoqueMinimo()),
                            i.status().name()
                    }).toList()
            );
            return csvResponse(csv, "relatorio-alertas.csv");
        }
        return ResponseEntity.ok(dados);
    }

    @GetMapping("/movimentacoes")
    public ResponseEntity<?> movimentacoes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(required = false) TipoMovimentacao tipo,
            HttpServletRequest request) {
        RelatorioMovimentacoes relatorio = relatorioService.movimentacoes(de, ate, tipo);
        if (aceitaCsv(request)) {
            String csv = CsvHelper.toCsv(
                    new String[]{"SKU", "Produto", "Tipo", "Origem", "Quantidade", "Realizado Em"},
                    relatorio.dados().stream().map(m -> new String[]{
                            m.produtoSku(), m.produtoNome(), m.tipo(), m.origem(),
                            String.valueOf(m.quantidade()), m.realizadoEm().toString()
                    }).toList()
            );
            return csvResponse(csv, "relatorio-movimentacoes.csv");
        }
        return ResponseEntity.ok(relatorio);
    }

    @GetMapping("/pedidos-compra")
    public ResponseEntity<?> pedidosCompra(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(required = false) StatusPedido status,
            HttpServletRequest request) {
        RelatorioPedidos relatorio = relatorioService.pedidos(de, ate, status);
        if (aceitaCsv(request)) {
            String csv = CsvHelper.toCsv(
                    new String[]{"ID", "Fornecedor", "Status", "Data Emissão", "Total Pedido"},
                    relatorio.dados().stream().map(p -> new String[]{
                            p.id().toString(), p.fornecedorNome(), p.status(),
                            p.dataEmissao().toString(), p.totalPedido().toPlainString()
                    }).toList()
            );
            return csvResponse(csv, "relatorio-pedidos-compra.csv");
        }
        return ResponseEntity.ok(relatorio);
    }

    private boolean aceitaCsv(HttpServletRequest request) {
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        return accept != null && accept.contains("text/csv");
    }

    private ResponseEntity<String> csvResponse(String csv, String filename) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(csv);
    }
}
