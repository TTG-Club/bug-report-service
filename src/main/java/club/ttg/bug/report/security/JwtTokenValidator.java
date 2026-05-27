package club.ttg.bug.report.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenValidator {

    private static final String USERNAME_CLAIM = "username";
    private static final String ROLES_CLAIM = "roles";
    private static final String AUTHORITIES_CLAIM = "authorities";

    private final JwtDecoder jwtDecoder;

    public Optional<JwtAuthenticatedUser> validateToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            String username = resolveUsername(jwt);
            List<String> roles = resolveRoles(jwt);

            if (username == null || username.isBlank()) {
                log.debug("JWT token is valid but does not contain a username");
                return Optional.empty();
            }

            return Optional.of(new JwtAuthenticatedUser(username, roles));
        } catch (JwtException e) {
            log.debug("JWT token validation failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private String resolveUsername(Jwt jwt) {
        String username = jwt.getClaimAsString(USERNAME_CLAIM);
        return username != null ? username : jwt.getSubject();
    }

    private List<String> resolveRoles(Jwt jwt) {
        Object rolesClaim = jwt.getClaims().get(ROLES_CLAIM);
        if (rolesClaim == null) {
            rolesClaim = jwt.getClaims().get(AUTHORITIES_CLAIM);
        }

        if (rolesClaim instanceof Collection<?> roles) {
            return roles.stream()
                    .map(String::valueOf)
                    .map(this::normalizeRole)
                    .filter(role -> !role.isBlank())
                    .toList();
        }

        if (rolesClaim instanceof String roles) {
            return Arrays.stream(roles.split(","))
                    .map(this::normalizeRole)
                    .filter(role -> !role.isBlank())
                    .toList();
        }

        return List.of();
    }

    private String normalizeRole(String role) {
        String normalizedRole = role.trim();
        if (normalizedRole.startsWith("ROLE_")) {
            return normalizedRole.substring("ROLE_".length());
        }
        return normalizedRole;
    }
}
