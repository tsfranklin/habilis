package com.habilis.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para el registro de nuevos usuarios
 */
public class RegistroRequest {

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    private String nombreCompleto;

    @NotBlank(message = "El móvil es obligatorio")
    @Pattern(regexp = "^\\+34[6-9][0-9]{8}$", message = "El formato del móvil debe ser +34XXXXXXXXX")
    private String movil;

    @NotBlank(message = "El correo electrónico es oligatorio")
    @Email(message = "El formato del correo electrónico no es válido")
    @Size(max = 100, message = "El correo no puede exceder 100 caracteres")
    private String correoElectronico;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 50, message = "La contraseña debe tener entre 8 y 50 caracteres")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "La contraseña debe contener al menos una mayúscula, una minúscula y un número")
    private String contrasena;

    // Constructores
    public RegistroRequest() {
    }

    public RegistroRequest(String nombreCompleto, String movil, String correoElectronico, String contrasena) {
        this.nombreCompleto = nombreCompleto;
        this.movil = movil;
        this.correoElectronico = correoElectronico;
        this.contrasena = contrasena;
    }

    // Getters y Setters
    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getMovil() {
        return movil;
    }

    public void setMovil(String movil) {
        this.movil = movil;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
