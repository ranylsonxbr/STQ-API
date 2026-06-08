package com.example.Stq.frontend.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginWebController {

    @GetMapping("/web/login")
    public String login() {
        return "login";
    }
}
