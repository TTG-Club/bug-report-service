package club.ttg.bug.report.config;

import club.ttg.bug.report.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:*}")
    private List<String> allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        if (allowedOrigins.contains("*")) {
            configuration.setAllowedOriginPatterns(List.of("*"));
        } else {
            configuration.setAllowedOrigins(allowedOrigins);
            configuration.setAllowCredentials(true);
        }
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/bugs").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/bugs/statuses").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/bugs/types").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/bugs/stats").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/bugs/*/screenshot").permitAll()
                        // Свои баг-репорты доступны любому авторизованному пользователю.
                        // Правило обязано стоять ДО общего "/api/v1/bugs/**": Spring берёт
                        // первый совпавший матчер, иначе автор получит 403 на собственные баги.
                        .requestMatchers(HttpMethod.GET, "/api/v1/bugs/my", "/api/v1/bugs/my/**").authenticated()
                        // Отзыв своей записи — тоже любому авторизованному и тоже до общих правил.
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/bugs/my/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/bugs", "/api/v1/bugs/**").hasAnyRole("ADMIN", "MODERATOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/bugs/*/status").hasAnyRole("ADMIN", "MODERATOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/bugs/*/type").hasAnyRole("ADMIN", "MODERATOR")
                        .anyRequest().authenticated()
                )
                // Без токена ручка отзыва отвечает 401 (контракт сайта), остальные ручки — 403, как раньше.
                // Второе правило обязательно: единственный зарегистрированный entry point Spring
                // применяет ко ВСЕМ запросам, и 401 получили бы все защищённые ручки.
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.DELETE, "/api/v1/bugs/my/*"))
                        .defaultAuthenticationEntryPointFor(
                                new Http403ForbiddenEntryPoint(),
                                AnyRequestMatcher.INSTANCE))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
