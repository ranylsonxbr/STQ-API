package com.example.Stq.frontend.web;

import com.example.Stq.fornecedor.application.services.FornecedorService;
import com.example.Stq.fornecedor.domain.FornecedorFiltro;
import com.example.Stq.frontend.form.ItemPedidoForm;
import com.example.Stq.frontend.form.PedidoForm;
import com.example.Stq.frontend.support.UsuarioLogado;
import com.example.Stq.pedidocompra.application.dto.CriarPedidoRequest;
import com.example.Stq.pedidocompra.application.dto.ItemRequest;
import com.example.Stq.pedidocompra.application.services.PedidoCompraService;
import com.example.Stq.pedidocompra.domain.PedidoCompraFiltro;
import com.example.Stq.pedidocompra.domain.StatusPedido;
import com.example.Stq.produto.application.services.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/web/pedidos")
@RequiredArgsConstructor
public class PedidoCompraWebController {

    private final PedidoCompraService pedidoCompraService;
    private final FornecedorService fornecedorService;
    private final ProdutoService produtoService;
    private final UsuarioLogado usuarioLogado;

    // -------------------------------------------------------------------------
    // Lista
    // -------------------------------------------------------------------------

    @GetMapping
    public String listar(
            @RequestParam(required = false) StatusPedido status,
            @PageableDefault(size = 20, sort = "criadoEm") Pageable pageable,
            Model model) {
        var filtro = new PedidoCompraFiltro(null, status, null, null);
        model.addAttribute("pedidos", pedidoCompraService.listar(filtro, pageable));
        model.addAttribute("filtroStatus", status);
        model.addAttribute("statusPedido", StatusPedido.values());
        return "pedido/lista";
    }

    // -------------------------------------------------------------------------
    // Detalhe
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    public String detalhe(@PathVariable UUID id, Model model) {
        model.addAttribute("pedido", pedidoCompraService.buscarPorId(id));
        model.addAttribute("itemPedidoForm",
                new ItemPedidoForm(null, null, null, null));
        model.addAttribute("produtos",
                produtoService.listar(null, true, null, PageRequest.of(0, 200)).getContent());
        return "pedido/detalhe";
    }

    // -------------------------------------------------------------------------
    // Novo pedido
    // -------------------------------------------------------------------------

    @GetMapping("/novo")
    public String novoForm(Model model) {
        model.addAttribute("pedidoForm",
                new PedidoForm(null, null, null, null, null, null, null, null));
        model.addAttribute("fornecedores",
                fornecedorService.listar(new FornecedorFiltro(null, null, true),
                        PageRequest.of(0, 200)).getContent());
        model.addAttribute("produtos",
                produtoService.listar(null, true, null, PageRequest.of(0, 200)).getContent());
        return "pedido/form";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public String criar(
            @Valid @ModelAttribute("pedidoForm") PedidoForm form,
            BindingResult bindingResult,
            Authentication auth,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("fornecedores",
                    fornecedorService.listar(new FornecedorFiltro(null, null, true),
                            PageRequest.of(0, 200)).getContent());
            model.addAttribute("produtos",
                    produtoService.listar(null, true, null, PageRequest.of(0, 200)).getContent());
            return "pedido/form";
        }
        var itemRequest = new ItemRequest(
                form.itemProdutoId(),
                form.itemVariacaoId(),
                form.itemQuantidade(),
                form.itemPrecoUnitario()
        );
        var request = new CriarPedidoRequest(
                form.fornecedorId(),
                form.dataEmissao(),
                form.dataPrevisaoEntrega(),
                form.observacao(),
                List.of(itemRequest)
        );
        try {
            var pedido = pedidoCompraService.criar(request, usuarioLogado.obterUsuarioId(auth));
            redirectAttributes.addFlashAttribute("sucesso", "Pedido criado com sucesso.");
            return "redirect:/web/pedidos/" + pedido.id();
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/web/pedidos/novo";
        }
    }

    // -------------------------------------------------------------------------
    // Adicionar item
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/itens")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public String adicionarItem(
            @PathVariable UUID id,
            @Valid @ModelAttribute("itemPedidoForm") ItemPedidoForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pedido", pedidoCompraService.buscarPorId(id));
            model.addAttribute("produtos",
                    produtoService.listar(null, true, null, PageRequest.of(0, 200)).getContent());
            return "pedido/detalhe";
        }
        try {
            var itemRequest = new ItemRequest(
                    form.produtoId(),
                    form.variacaoId(),
                    form.quantidade(),
                    form.precoUnitario()
            );
            pedidoCompraService.adicionarItem(id, itemRequest);
            redirectAttributes.addFlashAttribute("sucesso", "Item adicionado com sucesso.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/web/pedidos/" + id;
    }

    // -------------------------------------------------------------------------
    // Remover item
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/itens/{itemId}/remover")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public String removerItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            RedirectAttributes redirectAttributes) {
        try {
            pedidoCompraService.removerItem(id, itemId);
            redirectAttributes.addFlashAttribute("sucesso", "Item removido com sucesso.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/web/pedidos/" + id;
    }

    // -------------------------------------------------------------------------
    // Transições de status
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/enviar")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public String enviar(
            @PathVariable UUID id,
            RedirectAttributes redirectAttributes) {
        try {
            pedidoCompraService.enviar(id);
            redirectAttributes.addFlashAttribute("sucesso", "Pedido enviado para aprovação.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/web/pedidos/" + id;
    }

    @PostMapping("/{id}/aprovar")
    @PreAuthorize("hasRole('ADMIN')")
    public String aprovar(
            @PathVariable UUID id,
            RedirectAttributes redirectAttributes) {
        try {
            pedidoCompraService.aprovar(id);
            redirectAttributes.addFlashAttribute("sucesso", "Pedido aprovado com sucesso.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/web/pedidos/" + id;
    }

    @PostMapping("/{id}/receber")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public String receber(
            @PathVariable UUID id,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            pedidoCompraService.receber(id, usuarioLogado.obterUsuarioId(auth));
            redirectAttributes.addFlashAttribute("sucesso", "Pedido recebido. Entradas registradas no estoque.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/web/pedidos/" + id;
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public String cancelar(
            @PathVariable UUID id,
            RedirectAttributes redirectAttributes) {
        try {
            pedidoCompraService.cancelar(id);
            redirectAttributes.addFlashAttribute("sucesso", "Pedido cancelado.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/web/pedidos/" + id;
    }
}
