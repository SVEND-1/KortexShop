package org.example.kortex.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/login")
    public String login(Model model) {
        return "forward:/loginForm.html";
    }

    @GetMapping("/codeEmail")
    public String code(Model model) {
        return "forward:/codeFromEmailForm.html";
    }

    @GetMapping("/forgotPassword")
    public String forgot(Model model) {
        return "forward:/forgotPasswordForm.html";
    }

    @GetMapping("/recoveryPassword")
    public String recovery(Model model) {
        return "forward:/recoveryPasswordForm.html";
    }

    @GetMapping("/register")
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
        return "forward:/profile.html";
    }

    @GetMapping("/cart")
    public String cart(){return "forward:/cartForm.html";}

    @GetMapping("/productForm")
    public String productForm() {return "forward:/productForm.html";}

}
