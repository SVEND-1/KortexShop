package org.example.kortex.web;

import org.springframework.stereotype.Controller;
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
        return "forward:/sellerForm.html";
    }

    @GetMapping("/admin")
    public String admin() {
        return "forward:/admin.html";
    }

    @GetMapping()
    public String mainForm() {
        return "forward:/mainForm.html";
    }

    @GetMapping("/payments")
    public String payments() {
        return "forward:/payment.html";
    }

    @GetMapping("/profile")
    public String profile() {
        return "forward:/profile.html";
    }

    @GetMapping("/cart")
    public String cart() {
        return "forward:/cartForm.html";
    }

    @GetMapping("/productForm")
    public String productForm() {
        return "forward:/productForm.html";
    }

    @GetMapping("/courier")
    public String courier() {
        return "forward:/courier.html";
    }

    @GetMapping("/checkout")
    public String checkout(){return "forward:/checkout.html";}

    @GetMapping("/error")
    public String getErrorPage() {
        return "forward:/error.html";
    }

}
