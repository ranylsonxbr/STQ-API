package com.example.Stq.frontend.web;

import com.example.Stq.fornecedor.application.dto.FornecedorRequest;
import com.example.Stq.fornecedor.application.services.FornecedorService;
import com.example.Stq.fornecedor.domain.FornecedorFiltro;
import com.example.Stq.fornecedor.domain.exception.CnpjDuplicadoException;
import com.example.Stq.fornecedor.domain.exception.CnpjInvalidoException;
import com.example.Stq.fornecedor.domain.exception.EdicaoCnpjNaoPermitidaException;
import com.example.Stq.frontend.form.FornecedorForm;
import com.example.Stq.frontend.support.UsuarioLogado;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/web/fornecedores")
@RequiredArgsConstructor
public class FornecedorWebController {

    private final FornecedorService fornecedorService;
    private final UsuarioLogado usuarioLogado;

    @GetMapping
    public String listar(
            @RequestParam(required = false) String razaoSocial,
            @RequestParam(required = false) String cnpj,
            @RequestParam(required = false) Boolean ativo,
            @PageableDefault(size = 20, sort = "razaoSocial") Pageable pageable,
            Model model) {
        var filtro = new FornecedorFiltro(razaoSocial, cnpj, ativo);
        model.addAttribute("fornecedores", fornecedorService.listar(filtro, pageable));
        model.addAttribute("filtroRazaoSocial", razaoSocial);
        model.addAttribute("filtroCnpj", cnpj);
        model.addAttribute("filtroAtivo", ativo);
        return "fornecedor/lista";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable UUID id, Model model) {
        model.addAttribute("fornecedor", fornecedorService.buscarPorId(id));
        return "fornecedor/detalhe";
    }

    @GetMapping("/novo")
    public String novoForm(Model model) {
        model.addAttribute("fornecedorForm", new FornecedorForm(null, null, null, null, null));
        model.addAttribute("editando", false);
        return "fornecedor/form";
    }

    @PostMapping
    public String criar(
            @Valid FornecedorForm fornecedorForm,
            BindingResult bindingResult,
            Model model,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("editando", false);
            return "fornecedor/form";
        }
        UUID usuarioId = usuarioLogado.obterUsuarioId(auth);
        var request = new FornecedorRequest(
                fornecedorForm.razaoSocial(),
                fornecedorForm.cnpj(),
                fornecedorForm.email(),
                fornecedorForm.telefone(),
                fornecedorForm.contato()
        );
        try {
            var response = fornecedorService.criar(request, usuarioId);
            redirectAttributes.addFlashAttribute("sucesso", "Fornecedor criado com sucesso.");
            return "redirect:/web/fornecedores/" + response.id();
        } catch (CnpjInvalidoException | CnpjDuplicadoException ex) {
            bindingResult.rejectValue("cnpj", "invalido", ex.getMessage());
            model.addAttribute("editando", false);
            return "fornecedor/form";
        }
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable UUID id, Model model) {
        var fornecedor = fornecedorService.buscarPorId(id);
        var form = new FornecedorForm(
                fornecedor.razaoSocial(),
                fornecedor.cnpj(),
                fornecedor.email(),
                fornecedor.telefone(),
                fornecedor.contato()
        );
        model.addAttribute("fornecedorForm", form);
        model.addAttribute("fornecedorId", id);
        model.addAttribute("editando", true);
        return "fornecedor/form";
    }

    @PostMapping("/{id}")
    public String atualizar(
            @PathVariable UUID id,
            @Valid FornecedorForm fornecedorForm,
            BindingResult bindingResult,
            Model model,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("fornecedorId", id);
            model.addAttribute("editando", true);
            return "fornecedor/form";
        }
        UUID usuarioId = usuarioLogado.obterUsuarioId(auth);
        var perfil = usuarioLogado.obterPerfil(auth);
        var request = new FornecedorRequest(
                fornecedorForm.razaoSocial(),
                fornecedorForm.cnpj(),
                fornecedorForm.email(),
                fornecedorForm.telefone(),
                fornecedorForm.contato()
        );
        try {
            fornecedorService.atualizar(id, request, usuarioId, perfil);
            redirectAttributes.addFlashAttribute("sucesso", "Fornecedor atualizado com sucesso.");
            return "redirect:/web/fornecedores/" + id;
        } catch (CnpjInvalidoException | CnpjDuplicadoException | EdicaoCnpjNaoPermitidaException ex) {
            bindingResult.rejectValue("cnpj", "invalido", ex.getMessage());
            model.addAttribute("fornecedorId", id);
            model.addAttribute("editando", true);
            return "fornecedor/form";
        }
    }

    @PostMapping("/{id}/desativar")
    public String desativar(
            @PathVariable UUID id,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            UUID usuarioId = usuarioLogado.obterUsuarioId(auth);
            fornecedorService.desativar(id, usuarioId);
            redirectAttributes.addFlashAttribute("sucesso", "Fornecedor desativado com sucesso.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/web/fornecedores";
    }

    @PostMapping("/{id}/reativar")
    public String reativar(
            @PathVariable UUID id,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            UUID usuarioId = usuarioLogado.obterUsuarioId(auth);
            fornecedorService.reativar(id, usuarioId);
            redirectAttributes.addFlashAttribute("sucesso", "Fornecedor reativado com sucesso.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/web/fornecedores/" + id;
    }
}
