package com.bugtracker.security;

import lombok.Data;

import java.util.List;

/**
 * DTO ответа от внешнего сервиса авторизации.
 * Содержит информацию о пользователе и его ролях.
 */
@Data
public class AuthValidationResponse {

    /**
     * Логин пользователя.
     */
    private String username;

    /**
     * Список ролей пользователя.
     */
    private List<String> roles;

    /**
     * Признак валидности токена.
     */
    private boolean valid;
}
