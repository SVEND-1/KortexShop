package org.example.kortex.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/loginForm")
    public String login(Model model) {
        return "forward:/loginForm.html";
    }

    @GetMapping("/codeFromEmailForm")
    public String code(Model model) {
        return "forward:/codeFromEmailForm.html";
    }

    @GetMapping("/forgotPasswordForm")
    public String forgot(Model model) {
        return "forward:/forgotPasswordForm.html";
    }

    @GetMapping("/recoveryPasswordForm")
    public String recovery(Model model) {
        return "forward:/recoveryPasswordForm.html";
    }

    @GetMapping("/registerForm")
    public String register(Model model) {
        return "forward:/registerForm.html";
    }

    @GetMapping("/seller")
    public String seller(Model model) {
        return "forward:/seller.html";
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        return "forward:/admin.html";
    }

    @GetMapping()
    public String mainForm(Model model) {
        return "forward:/mainForm.html";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        return "forward:/mainForm.html";
    }
}
