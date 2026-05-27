package club.ttg.bug.report.security;

import java.util.List;

public record JwtAuthenticatedUser(String username, List<String> roles) {
}
