package com.habilis.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Pedido - Representa la tabla 'pedidos'
 * Cabecera de la compra
 */
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotNull(message = "La fecha del pedido es obligatoria")
    @Column(name = "fecha_pedido", nullable = false)
    private LocalDateTime fechaPedido;

    @NotNull(message = "El total del pedido es obligatorio")
    @DecimalMin(value = "0.00", message = "El total no puede ser negativo")
    @Column(name = "total_pedido", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPedido;

    @NotNull(message = "El estado del pedido es obligatorio")
    @Column(name = "estado", nullable = false, length = 20)
    private String estado; // 'PENDIENTE', 'ENVIADO', 'COMPLETADO', 'CANCELADO'

    // Relación con DetallePedido (un pedido tiene muchos detalles)
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    // Constructores
    public Pedido() {
        this.fechaPedido = LocalDateTime.now();
        this.estado = "PENDIENTE";
    }

    public Pedido(Usuario usuario, BigDecimal totalPedido) {
        this.usuario = usuario;
        this.fechaPedido = LocalDateTime.now();
        this.totalPedido = totalPedido;
        this.estado = "PENDIENTE";
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public BigDecimal getTotalPedido() {
        return totalPedido;
    }

    public void setTotalPedido(BigDecimal totalPedido) {
        this.totalPedido = totalPedido;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles = detalles;
    }
}
