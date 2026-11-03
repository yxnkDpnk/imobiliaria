package com.example.primeiro_projeto.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtFilter extends OncePerRequestFilter {

	private final JwtService jwtService;

	public JwtFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (authorization == null) {
			filterChain.doFilter(request, response);
			return;
		}

		if (!authorization.startsWith("Bearer ") || authorization.length() <= 7) {
			responderNaoAutorizado(response);
			return;
		}

		String token = authorization.substring(7).trim();
		if (token.isEmpty()) {
			responderNaoAutorizado(response);
			return;
		}

		Claims claims;
		String login;
		String perfil;
		try {
			// AULA-JWT: Só usamos os dados do usuário depois de validar a assinatura e a expiração.
			claims = jwtService.validarToken(token);
			login = claims.getSubject();
			perfil = claims.get("role", String.class);
		} catch (JwtException | IllegalArgumentException e) {
			responderNaoAutorizado(response);
			return;
		}

		if (login == null || login.isBlank()
				|| !("ADMIN".equals(perfil) || "USER".equals(perfil))) {
			responderNaoAutorizado(response);
			return;
		}

		// AULA-JWT: O prefixo ROLE_ permite que hasRole reconheça o perfil do token.
		var autenticacao = new UsernamePasswordAuthenticationToken(
				login,
				null,
				List.of(new SimpleGrantedAuthority("ROLE_" + perfil)));
		autenticacao.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		SecurityContextHolder.getContext().setAuthentication(autenticacao);

		filterChain.doFilter(request, response);
	}

	private void responderNaoAutorizado(HttpServletResponse response) throws IOException {
		SecurityContextHolder.clearContext();
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write("{\"erro\":\"Token ausente ou inválido\"}");
	}
}
