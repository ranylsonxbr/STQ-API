package com.example.Stq.frontend.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErroWebController {

    @GetMapping("/web/acesso-negado")
    public String acessoNegado() {
        return "erro/acesso-negado";
    }
}
