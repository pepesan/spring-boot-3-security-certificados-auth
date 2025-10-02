package com.cursosdedesarrollo.springboot3securitycertificadosauth.services;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyStore;
import java.security.SecureRandom;

@Service
public class ClientMtlsService {

    private final HttpClient httpClient;

    public ClientMtlsService() {
        try {
            // 1) Keystore del CLIENTE (contiene clave privada + cert cliente)
            char[] keyPass = "changeit".toCharArray();
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (InputStream is = new ClassPathResource("client.p12").getInputStream()) {
                keyStore.load(is, keyPass);
            }
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, keyPass);

            // 2) Truststore del CLIENTE (confía en la CA que firmó al SERVIDOR)
            char[] trustPass = "changeit".toCharArray();
            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            try (InputStream is = new ClassPathResource("ca.p12").getInputStream()) {
                trustStore.load(is, trustPass);
            }
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            // 3) SSLContext con KeyManagers (cliente) + TrustManagers (CA del server)
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

            // 4) HttpClient con ese SSLContext
            this.httpClient = HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .build();

        } catch (Exception e) {
            throw new IllegalStateException("No se pudo inicializar el cliente mTLS", e);
        }
    }

    public String callHello() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://localhost:8443/hello"))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("Fallo llamando a /hello como cliente mTLS", e);
        }
    }
}
