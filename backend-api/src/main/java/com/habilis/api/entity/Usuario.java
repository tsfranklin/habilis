package com.habilis.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Usuario - Representa la tabla 'usuarios'
 * Cumple con los requisitos mínimos del proyecto académico
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    @Column(name = "nombre_completo", nullable = false, length = 50)
    private String nombreCompleto;

    @NotBlank(message = "El móvil es obligatorio")
    @Size(max = 20, message = "El móvil no puede exceder 20 caracteres")
    @Column(name = "movil", nullable = false, length = 20)
    private String movil;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico no es válido")
    @Size(max = 100, message = "El correo no puede exceder 100 caracteres")
    @Column(name = "correo_electronico", nullable = false, unique = true, length = 100)
    private String correoElectronico;

    @NotBlank(message = "El tipo de usuario es obligatorio")
    @Column(name = "tipo_usuario", nullable = false, length = 20)
    private String tipoUsuario; // 'ADMIN' o 'USER'

    @NotBlank(message = "La contraseña es obligatoria")
    @Column(name = "contrasena", nullable = false, length = 255)
    private String contrasena; // Encriptada con BCrypt

    @Column(name = "token_recuperacion", length = 100)
    private String tokenRecuperacion;

    @Column(name = "cuenta_activa", nullable = false)
    private Boolean cuentaActiva = false;

    // Relación con Pedidos (un usuario puede tener muchos pedidos)
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pedido> pedidos = new ArrayList<>();

    // Constructores
    public Usuario() {
    }

    public Usuario(String nombreCompleto, String movil, String correoElectronico, 
                   String tipoUsuario, String contrasena) {
        this.nombreCompleto = nombreCompleto;
        this.movil = movil;
        this.correoElectronico = correoElectronico;
        this.tipoUsuario = tipoUsuario;
        this.contrasena = contrasena;
        this.cuentaActiva = false;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getTokenRecuperacion() {
        return tokenRecuperacion;
    }

    public void setTokenRecuperacion(String tokenRecuperacion) {
        this.tokenRecuperacion = tokenRecuperacion;
    }

    public Boolean getCuentaActiva() {
        return cuentaActiva;
    }

    public void setCuentaActiva(Boolean cuentaActiva) {
        this.cuentaActiva = cuentaActiva;
    }

    public List<Pedido> getPedidos() {
        return pedidos;
    }

    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }
}
