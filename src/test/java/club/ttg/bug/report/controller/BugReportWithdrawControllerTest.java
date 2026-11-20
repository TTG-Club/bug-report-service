package club.ttg.bug.report.controller;

import club.ttg.bug.report.config.SecurityConfig;
import club.ttg.bug.report.exception.BugReportNotFoundException;
import club.ttg.bug.report.exception.BugReportNotWithdrawableException;
import club.ttg.bug.report.ratelimit.RateLimiter;
import club.ttg.bug.report.security.JwtAuthenticatedUser;
import club.ttg.bug.report.security.JwtTokenValidator;
import club.ttg.bug.report.service.BugReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * DELETE /api/v1/bugs/my/{id}: правила безопасности и коды ответа.
 * Токен проверяется настоящим JwtAuthenticationFilter, подменён только валидатор.
 */
@WebMvcTest(BugReportController.class)
@Import(SecurityConfig.class)
class BugReportWithdrawControllerTest {

    private static final String USER_TOKEN = "user-token";
    private static final String ADMIN_TOKEN = "admin-token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BugReportService bugReportService;

    @MockitoBean
    private RateLimiter rateLimiter;

    @MockitoBean
    private JwtTokenValidator jwtTokenValidator;

    @BeforeEach
    void setUp() {
        when(jwtTokenValidator.validateToken(anyString())).thenReturn(Optional.empty());
        when(jwtTokenValidator.validateToken(USER_TOKEN))
                .thenReturn(Optional.of(new JwtAuthenticatedUser("author", List.of("USER"))));
        when(jwtTokenValidator.validateToken(ADMIN_TOKEN))
                .thenReturn(Optional.of(new JwtAuthenticatedUser("admin", List.of("ADMIN"))));
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    void withoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/bugs/my/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(bugReportService);
    }

    @Test
    void invalidTokenIsUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/bugs/my/{id}", UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, bearer("broken")))
                .andExpect(status().isUnauthorized());

        verify(bugReportService, never()).withdrawByAuthor(any(), anyString());
    }

    @Test
    void regularUserWithdrawsOwnReport() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/bugs/my/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_TOKEN)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(bugReportService).withdrawByAuthor(id, "author");
    }

    @Test
    void adminWithdrawsOnlyAsAuthor() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/bugs/my/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(ADMIN_TOKEN)))
                .andExpect(status().isNoContent());

        verify(bugReportService).withdrawByAuthor(id, "admin");
    }

    @Test
    void notFoundOrSomeoneElsesReportIs404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new BugReportNotFoundException("Баг-репорт не найден: " + id))
                .when(bugReportService).withdrawByAuthor(id, "author");

        mockMvc.perform(delete("/api/v1/bugs/my/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_TOKEN)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Баг-репорт не найден: " + id))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void reportInWorkIs409() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new BugReportNotWithdrawableException("Баг-репорт уже взят в работу, отозвать его нельзя"))
                .when(bugReportService).withdrawByAuthor(id, "author");

        mockMvc.perform(delete("/api/v1/bugs/my/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(USER_TOKEN)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Баг-репорт уже взят в работу, отозвать его нельзя"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
