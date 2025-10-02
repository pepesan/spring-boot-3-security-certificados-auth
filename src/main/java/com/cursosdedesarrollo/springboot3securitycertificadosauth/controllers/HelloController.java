package com.cursosdedesarrollo.springboot3securitycertificadosauth.controllers;

import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HelloController {

    @GetMapping("/hello")
    String hello(Principal principal) {
        return "Hola, " + principal.getName();
    }
}
