package com.bugtracker.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Фильтр аутентификации по JWT-токену.
 * <p>
 * Извлекает токен из заголовка Authorization, валидирует его через внешний сервис
 * авторизации и устанавливает контекст безопасности Spring Security.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final ExternalAuthClient externalAuthClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());

            externalAuthClient.validateToken(token).ifPresent(authResponse -> {
                List<SimpleGrantedAuthority> authorities = authResponse.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .toList();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                authResponse.getUsername(),
                                null,
                                authorities
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Пользователь '{}' аутентифицирован с ролями: {}",
                        authResponse.getUsername(), authResponse.getRoles());
            });
        }

        filterChain.doFilter(request, response);
    }
}
