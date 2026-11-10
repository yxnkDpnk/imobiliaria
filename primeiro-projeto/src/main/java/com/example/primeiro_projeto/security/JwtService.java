package com.example.primeiro_projeto.security;

import java.util.Date;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey chave;

    // Recebe a chave configurada no application.properties
    public JwtService(@Value("${jwt.secret}") String segredo) {
        this.chave = Keys.hmacShaKeyFor(
            Decoders.BASE64.decode(segredo)
        );
    }

    // Cria um token JWT para o usuário
    public String gerarToken(String login, String perfil) {

        Date agora = new Date();

        Date expiracao = new Date(
            agora.getTime() + 3600000
        );

        return Jwts.builder()
            .subject(login)
            .claim("role", perfil)
            .claim("tipo", perfil)
            .issuedAt(agora)
            .expiration(expiracao)
            .signWith(chave, Jwts.SIG.HS256)
                .compact();
    }

            public Claims validarToken(String token) {
            // AULA-JWT: A chave confere a assinatura e o parser também rejeita tokens expirados.
            return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            }
}
