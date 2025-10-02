package com.cursosdedesarrollo.springboot3securitycertificadosauth.controllers;

import com.cursosdedesarrollo.springboot3securitycertificadosauth.services.ClientMtlsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClientController {

    private final ClientMtlsService clientMtlsService;

    public ClientController(ClientMtlsService clientMtlsService) {
        this.clientMtlsService = clientMtlsService;
    }

    @GetMapping("/client-hello")
    public String clientHello() {
        // Hace una llamada HTTPS mTLS a /hello (mismo proceso)
        return clientMtlsService.callHello();
    }
}

