package com.example.Stq.frontend.web;

import com.example.Stq.frontend.form.AjusteForm;
import com.example.Stq.frontend.form.EntradaForm;
import com.example.Stq.frontend.form.SaidaForm;
import com.example.Stq.frontend.form.TransferenciaForm;
import com.example.Stq.frontend.support.UsuarioLogado;
import com.example.Stq.movimentacao.application.dto.AjusteRequest;
import com.example.Stq.movimentacao.application.dto.EntradaRequest;
import com.example.Stq.movimentacao.application.dto.SaidaRequest;
import com.example.Stq.movimentacao.application.dto.TransferenciaRequest;
import com.example.Stq.movimentacao.application.services.EstoqueService;
import com.example.Stq.movimentacao.application.services.MovimentacaoService;
import com.example.Stq.movimentacao.domain.EstoqueFiltro;
import com.example.Stq.movimentacao.domain.MovimentacaoFiltro;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class MovimentacaoWebController {

    private final MovimentacaoService movimentacaoService;
    private final EstoqueService estoqueService;
    private final UsuarioLogado usuarioLogado;

    // -------------------------------------------------------------------------
    // Histórico de movimentações
    // -------------------------------------------------------------------------

    @GetMapping("/web/movimentacoes")
    public String listar(
            @PageableDefault(size = 20, sort = "realizadoEm") Pageable pageable,
            Model model) {
        var filtro = new MovimentacaoFiltro(null, null, null, null);
        model.addAttribute("movimentacoes", movimentacaoService.listar(filtro, pageable));
        return "movimentacao/lista";
    }

    // -------------------------------------------------------------------------
    // Saldo de estoque
    // -------------------------------------------------------------------------

    @GetMapping("/web/estoque")
    public String estoque(
            @PageableDefault(size = 20) Pageable pageable,
            Model model) {
        var filtro = new EstoqueFiltro(null);
        model.addAttribute("estoques", estoqueService.consultarSaldo(filtro, pageable));
        return "movimentacao/estoque";
    }

    // -------------------------------------------------------------------------
    // Entrada
    // -------------------------------------------------------------------------

    @GetMapping("/web/movimentacoes/entrada")
    public String entradaForm(Model model) {
        model.addAttribute("entradaForm", new EntradaForm(null, null, null, null, null));
        return "movimentacao/entrada";
    }

    @PostMapping("/web/movimentacoes/entrada")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public String registrarEntrada(
            @Valid @ModelAttribute("entradaForm") EntradaForm form,
            BindingResult bindingResult,
            Authentication auth,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "movimentacao/entrada";
        }
        var request = new EntradaRequest(
                form.produtoId(),
                form.variacaoId(),
                form.localizacao(),
                form.quantidade(),
                form.observacao()
        );
        movimentacaoService.registrarEntrada(request, usuarioLogado.obterUsuarioId(auth));
        redirectAttributes.addFlashAttribute("sucesso", "Entrada registrada com sucesso.");
        return "redirect:/web/movimentacoes";
    }

    // -------------------------------------------------------------------------
    // Saída
    // -------------------------------------------------------------------------

    @GetMapping("/web/movimentacoes/saida")
    public String saidaForm(Model model) {
        model.addAttribute("saidaForm", new SaidaForm(null, null, null, null, null));
        return "movimentacao/saida";
    }

    @PostMapping("/web/movimentacoes/saida")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public String registrarSaida(
            @Valid @ModelAttribute("saidaForm") SaidaForm form,
            BindingResult bindingResult,
            Authentication auth,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "movimentacao/saida";
        }
        try {
            var request = new SaidaRequest(
                    form.produtoId(),
                    form.variacaoId(),
                    form.localizacao(),
                    form.quantidade(),
                    form.observacao()
            );
            movimentacaoService.registrarSaida(request, usuarioLogado.obterUsuarioId(auth));
            redirectAttributes.addFlashAttribute("sucesso", "Saída registrada com sucesso.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/web/movimentacoes";
    }

    // -------------------------------------------------------------------------
    // Transferência
    // -------------------------------------------------------------------------

    @GetMapping("/web/movimentacoes/transferencia")
    public String transferenciaForm(Model model) {
        model.addAttribute("transferenciaForm",
                new TransferenciaForm(null, null, null, null, null, null));
        return "movimentacao/transferencia";
    }

    @PostMapping("/web/movimentacoes/transferencia")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public String registrarTransferencia(
            @Valid @ModelAttribute("transferenciaForm") TransferenciaForm form,
            BindingResult bindingResult,
            Authentication auth,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "movimentacao/transferencia";
        }
        var request = new TransferenciaRequest(
                form.produtoId(),
                form.variacaoId(),
                form.localizacaoOrigem(),
                form.localizacaoDestino(),
                form.quantidade(),
                form.observacao()
        );
        movimentacaoService.registrarTransferencia(request, usuarioLogado.obterUsuarioId(auth));
        redirectAttributes.addFlashAttribute("sucesso", "Transferência registrada com sucesso.");
        return "redirect:/web/movimentacoes";
    }

    // -------------------------------------------------------------------------
    // Ajuste (somente ADMIN — RN-09)
    // -------------------------------------------------------------------------

    @GetMapping("/web/movimentacoes/ajuste")
    @PreAuthorize("hasRole('ADMIN')")
    public String ajusteForm(Model model) {
        model.addAttribute("ajusteForm", new AjusteForm(null, null, null, null, null));
        return "movimentacao/ajuste";
    }

    @PostMapping("/web/movimentacoes/ajuste")
    @PreAuthorize("hasRole('ADMIN')")
    public String registrarAjuste(
            @Valid @ModelAttribute("ajusteForm") AjusteForm form,
            BindingResult bindingResult,
            Authentication auth,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "movimentacao/ajuste";
        }
        var request = new AjusteRequest(
                form.produtoId(),
                form.variacaoId(),
                form.localizacao(),
                form.delta(),
                form.observacao()
        );
        movimentacaoService.registrarAjuste(request, usuarioLogado.obterUsuarioId(auth));
        redirectAttributes.addFlashAttribute("sucesso", "Ajuste de inventário registrado com sucesso.");
        return "redirect:/web/movimentacoes";
    }
}
