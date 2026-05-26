package com.bugtracker.config;

import com.bugtracker.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Конфигурация Spring Security.
 * <p>
 * Защищает эндпоинты получения списка и отдельного баг-репорта ролями ADMIN и MODERATOR.
 * Эндпоинт создания баг-репорта остается открытым для всех.
 * Аутентификация происходит через JWT-токен, валидируемый внешним сервисом.
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Настраивает цепочку фильтров безопасности.
     * POST /api/v1/bugs - доступен всем (создание бага).
     * GET /api/v1/bugs, GET /api/v1/bugs/{id} - только ADMIN или MODERATOR.
     * PATCH /api/v1/bugs/{id}/status - только ADMIN или MODERATOR.
     *
     * @param http объект конфигурации HTTP-безопасности
     * @return настроенная цепочка фильтров
     * @throws Exception при ошибке конфигурации
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/bugs").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/bugs/statuses").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/bugs", "/api/v1/bugs/**").hasAnyRole("ADMIN", "MODERATOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/bugs/*/status").hasAnyRole("ADMIN", "MODERATOR")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Бин WebClient для HTTP-запросов к внешнему сервису авторизации.
     *
     * @return экземпляр WebClient
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
