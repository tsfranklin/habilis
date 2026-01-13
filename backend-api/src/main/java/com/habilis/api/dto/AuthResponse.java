package com.habilis.api.dto;

/**
 * DTO para respuestas de autenticación
 */
public class AuthResponse {

    private boolean success;
    private String message;
    private Long userId;
    private String tipoUsuario;
    private Boolean requires2FA;

    // Constructores
    public AuthResponse() {
    }

    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public AuthResponse(boolean success, String message, Long userId, String tipoUsuario) {
        this.success = success;
        this.message = message;
        this.userId = userId;
        this.tipoUsuario = tipoUsuario;
    }

    // Getters y Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public Boolean getRequires2FA() {
        return requires2FA;
    }

    public void setRequires2FA(Boolean requires2FA) {
        this.requires2FA = requires2FA;
    }
}
