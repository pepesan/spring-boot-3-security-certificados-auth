package com.cursosdedesarrollo.springboot3securitycertificadosauth.services;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

@Service
public class WebClientMtlsService {

    private final WebClient webClient;

    public WebClientMtlsService() {
        try {
            // Keystore cliente (PKCS12)
            char[] keyPass = "changeit".toCharArray();
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (InputStream is = new ClassPathResource("client.p12").getInputStream()) {
                keyStore.load(is, keyPass);
            }
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, keyPass);

            // Truststore cliente (CA del servidor)
            char[] trustPass = "changeit".toCharArray();
            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            try (InputStream is = new ClassPathResource("ca.p12").getInputStream()) {
                trustStore.load(is, trustPass);
            }
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            var sslContext = SslContextBuilder.forClient()
                    .keyManager(kmf)
                    .trustManager(tmf)
                    .build();

            HttpClient httpClient = HttpClient.create().secure(ssl -> ssl.sslContext(sslContext));

            this.webClient = WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .build();

        } catch (Exception e) {
            throw new IllegalStateException("No se pudo inicializar WebClient mTLS", e);
        }
    }

    public String callHello() {
        return webClient.get()
                .uri("https://localhost:8443/hello")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}

