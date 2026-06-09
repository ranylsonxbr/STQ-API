package com.example.Stq.frontend.web;

import com.example.Stq.autenticacao.application.dto.CriarUsuarioRequest;
import com.example.Stq.autenticacao.application.dto.EditarUsuarioRequest;
import com.example.Stq.autenticacao.application.dto.RegistrarUsuarioRequest;
import com.example.Stq.autenticacao.application.services.UsuarioService;
import com.example.Stq.autenticacao.domain.Perfil;
import com.example.Stq.autenticacao.domain.exception.EmailJaCadastradoException;
import com.example.Stq.autenticacao.domain.exception.OperacaoNegadaException;
import com.example.Stq.frontend.form.CriarUsuarioForm;
import com.example.Stq.frontend.form.EditarUsuarioForm;
import com.example.Stq.frontend.form.RegistroForm;
import com.example.Stq.frontend.support.UsuarioLogado;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/web/usuarios")
@RequiredArgsConstructor
public class UsuarioWebController {

    private final UsuarioService usuarioService;
    private final UsuarioLogado usuarioLogado;

    private static final List<Perfil> PERFIS_DISPONIVEIS = Arrays.stream(Perfil.values())
            .filter(p -> p != Perfil.ADMIN)
            .toList();

    @GetMapping
    public String lista(@PageableDefault(size = 20) Pageable pageable, Model model) {
        model.addAttribute("page", usuarioService.listar(pageable));
        return "usuario/lista";
    }

    @GetMapping("/novo")
    public String novoForm(Model model) {
        model.addAttribute("usuarioForm", new CriarUsuarioForm("", "", "", "", Perfil.VISUALIZADOR));
        model.addAttribute("perfis", PERFIS_DISPONIVEIS);
        return "usuario/novo";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("usuarioForm") CriarUsuarioForm form,
                        BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (!form.senha().equals(form.confirmarSenha())) {
            result.rejectValue("confirmarSenha", "error.confirmarSenha", "As senhas não coincidem.");
        }
        if (result.hasErrors()) {
            model.addAttribute("perfis", PERFIS_DISPONIVEIS);
            return "usuario/novo";
        }
        try {
            usuarioService.criar(new CriarUsuarioRequest(form.nome(), form.email(), form.senha(), form.perfil()));
            redirectAttributes.addFlashAttribute("sucesso", "Usuário criado com sucesso.");
        } catch (EmailJaCadastradoException e) {
            result.rejectValue("email", "error.email", e.getMessage());
            model.addAttribute("perfis", PERFIS_DISPONIVEIS);
            return "usuario/novo";
        }
        return "redirect:/web/usuarios";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable UUID id, Model model) {
        var usuario = usuarioService.buscarPorId(id);
        model.addAttribute("usuarioId", id);
        model.addAttribute("usuarioForm", new EditarUsuarioForm(
                usuario.nome(), usuario.email(), Perfil.valueOf(usuario.perfil()), "", ""));
        model.addAttribute("perfis", PERFIS_DISPONIVEIS);
        return "usuario/editar";
    }

    @PostMapping("/{id}")
    public String editar(@PathVariable UUID id,
                         @Valid @ModelAttribute("usuarioForm") EditarUsuarioForm form,
                         BindingResult result, Model model,
                         Authentication auth, RedirectAttributes redirectAttributes) {
        if (form.novaSenha() != null && !form.novaSenha().isBlank()
                && !form.novaSenha().equals(form.confirmarNovaSenha())) {
            result.rejectValue("confirmarNovaSenha", "error.confirmarNovaSenha", "As senhas não coincidem.");
        }
        if (result.hasErrors()) {
            model.addAttribute("usuarioId", id);
            model.addAttribute("perfis", PERFIS_DISPONIVEIS);
            return "usuario/editar";
        }
        try {
            UUID idAdmin = usuarioLogado.obterUsuarioId(auth);
            usuarioService.editar(id,
                    new EditarUsuarioRequest(form.nome(), form.email(), form.perfil(), form.novaSenha()),
                    idAdmin);
            redirectAttributes.addFlashAttribute("sucesso", "Usuário atualizado com sucesso.");
        } catch (EmailJaCadastradoException e) {
            result.rejectValue("email", "error.email", e.getMessage());
            model.addAttribute("usuarioId", id);
            model.addAttribute("perfis", PERFIS_DISPONIVEIS);
            return "usuario/editar";
        } catch (OperacaoNegadaException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/usuarios";
    }

    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable UUID id, Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.desativar(id, usuarioLogado.obterUsuarioId(auth));
            redirectAttributes.addFlashAttribute("sucesso", "Usuário desativado.");
        } catch (OperacaoNegadaException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/web/usuarios";
    }

    @PostMapping("/{id}/reativar")
    public String reativar(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        usuarioService.reativar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Usuário reativado.");
        return "redirect:/web/usuarios";
    }

    // --- Registro público ---

    @GetMapping("/registro")
    public String registroForm(Model model) {
        model.addAttribute("registroForm", new RegistroForm("", "", "", ""));
        return "usuario/registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registroForm") RegistroForm form,
                            BindingResult result, RedirectAttributes redirectAttributes) {
        if (!form.senha().equals(form.confirmarSenha())) {
            result.rejectValue("confirmarSenha", "error.confirmarSenha", "As senhas não coincidem.");
        }
        if (result.hasErrors()) {
            return "usuario/registro";
        }
        try {
            usuarioService.registrar(new RegistrarUsuarioRequest(form.nome(), form.email(), form.senha()));
            redirectAttributes.addFlashAttribute("sucesso",
                    "Cadastro realizado! Faça login para acessar o sistema.");
        } catch (EmailJaCadastradoException e) {
            result.rejectValue("email", "error.email", e.getMessage());
            return "usuario/registro";
        }
        return "redirect:/web/login";
    }
}
