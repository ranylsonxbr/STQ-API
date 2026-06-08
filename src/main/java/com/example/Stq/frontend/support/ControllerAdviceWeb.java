package com.example.Stq.frontend.support;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;

import java.beans.PropertyEditorSupport;
import java.util.UUID;

@ControllerAdvice(basePackages = "com.example.Stq.frontend")
@RequiredArgsConstructor
public class ControllerAdviceWeb {

    private final UsuarioLogado usuarioLogado;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        binder.registerCustomEditor(UUID.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(text == null || text.isBlank() ? null : UUID.fromString(text));
            }
        });
    }

    @ModelAttribute
    public void injetarDadosUsuario(Authentication auth, org.springframework.ui.Model model) {
        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {
            try {
                model.addAttribute("nomeUsuario", usuarioLogado.obterNome(auth));
                model.addAttribute("perfilUsuario", usuarioLogado.obterPerfil(auth).name());
            } catch (IllegalStateException ignorado) {
                // principal incompatível — não injeta atributos
            }
        }
    }

    @ExceptionHandler(RuntimeException.class)
    public ModelAndView handleRuntimeException(RuntimeException ex) {
        ModelAndView mav = new ModelAndView("erro/generico");
        mav.addObject("mensagem", ex.getMessage());
        mav.setStatus(HttpStatus.OK);
        return mav;
    }
}
