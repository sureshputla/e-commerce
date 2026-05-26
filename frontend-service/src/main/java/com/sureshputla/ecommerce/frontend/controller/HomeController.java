package com.sureshputla.ecommerce.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /**
     * The API Gateway URL is injected into the Thymeleaf template so JavaScript
     * can call the correct backend gateway without hardcoding it.
     */
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("apiGatewayUrl", "http://localhost:8080");
        return "index";
    }
}

