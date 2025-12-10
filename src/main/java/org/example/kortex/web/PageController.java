package org.example.kortex.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/login")
    public String login() {
        return "forward:/loginForm.html";
    }

    @GetMapping("/codeEmail")
    public String code() {
        return "forward:/codeFromEmailForm.html";
    }

    @GetMapping("/forgotPassword")
    public String forgot() {
        return "forward:/forgotPasswordForm.html";
    }

    @GetMapping("/recoveryPassword")
    public String recovery() {
        return "forward:/recoveryPasswordForm.html";
    }

    @GetMapping("/register")
    public String register() {
        return "forward:/registerForm.html";
    }

    @GetMapping("/seller")
    public String seller() {
        return "forward:/seller.html";
    }

    @GetMapping("/admin")
    public String admin() {
        return "forward:/admin.html";
    }

    @GetMapping()
    public String mainForm() {
        return "forward:/mainForm.html";
    }

    @GetMapping("/profile")
    public String profile() {
        return "forward:/mainForm.html";
    }
}
