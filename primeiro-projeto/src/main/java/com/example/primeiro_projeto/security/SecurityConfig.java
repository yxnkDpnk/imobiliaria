package com.example.primeiro_projeto.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint(restAuthenticationEntryPoint())
                .accessDeniedHandler(restAccessDeniedHandler()))
            .authorizeHttpRequests(auth -> auth
                // AULA-JWT: O login e as consultas públicas não precisam de token.
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/auth/login").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/imoveis", "/imoveis/{id}").permitAll()
                // AULA-JWT: Visitantes podem pedir informações sem ter uma conta na imobiliária.
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/imoveis/sabermais/{id}").permitAll()
                // AULA-JWT: Alterar somente o status exige login, mas aceita ADMIN e USER.
                .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/imoveis/{id}/status").authenticated()
                // AULA-JWT: A área de usuários e operações que alteram dados ficam restritas a ADMIN.
                .requestMatchers("/users", "/users/**", "/email", "/email/**").hasRole("ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/**").hasRole("ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/**").hasRole("ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/**").hasRole("ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            // AULA-JWT: O filtro autentica a requisição antes das regras de autorização.
            .addFilterBefore(new JwtFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private org.springframework.security.web.AuthenticationEntryPoint restAuthenticationEntryPoint() {
        return (request, response, exception) -> {
            response.setStatus(401);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"erro\":\"Autenticação necessária\"}");
        };
    }

    private org.springframework.security.web.access.AccessDeniedHandler restAccessDeniedHandler() {
        return (request, response, exception) -> {
            response.setStatus(403);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"erro\":\"Acesso não permitido\"}");
        };
    }
}