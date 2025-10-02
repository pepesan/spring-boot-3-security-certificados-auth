package com.cursosdedesarrollo.springboot3securitycertificadosauth.controllers;

import com.cursosdedesarrollo.springboot3securitycertificadosauth.services.WebClientMtlsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class WebFluxClientController {
    private final WebClientMtlsService svc;
    WebFluxClientController(WebClientMtlsService svc) { this.svc = svc; }

    @GetMapping("/webclient-hello")
    String clientHello() { return svc.callHello(); }
}

