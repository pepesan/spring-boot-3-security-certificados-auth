package com.cursosdedesarrollo.springboot3securitycertificadosauth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Habilita autenticación X.509: extrae el principal del DN del cert
                .x509(x -> x
                        // Por defecto ya usa CN, pero lo mostramos para claridad:
                        .subjectPrincipalRegex("CN=(.*?)(?:,|$)")
                        // Mapear el CN extraído ("alice") a un UserDetails con sus roles
                        .userDetailsService(userDetailsService())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService() {
        // En X.509, la 'password' no se usa; se valida por certificado.
        UserDetails alice = User.withUsername("alice")
                .password("{noop}ignored") // no se utiliza
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(alice);
    }
}

