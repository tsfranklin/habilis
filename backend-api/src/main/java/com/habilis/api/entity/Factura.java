package com.habilis.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad Factura - Representa la tabla 'facturas'
 * Almacena las facturas generadas para cada pedido
 */
@Entity
@Table(name = "facturas")
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El código de factura es obligatorio")
    @Column(name = "codigo_factura", unique = true, nullable = false, length = 50)
    private String codigoFactura;

    @NotNull(message = "El pedido es obligatorio")
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotNull(message = "La fecha de emisión es obligatoria")
    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @NotNull(message = "El total es obligatorio")
    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "ruta_pdf", length = 255)
    private String rutaPdf;

    // Constructores
    public Factura() {
        this.fechaEmision = LocalDateTime.now();
    }

    public Factura(String codigoFactura, Pedido pedido, Usuario usuario, BigDecimal total) {
        this.codigoFactura = codigoFactura;
        this.pedido = pedido;
        this.usuario = usuario;
        this.total = total;
        this.fechaEmision = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoFactura() {
        return codigoFactura;
    }

    public void setCodigoFactura(String codigoFactura) {
        this.codigoFactura = codigoFactura;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getRutaPdf() {
        return rutaPdf;
    }

    public void setRutaPdf(String rutaPdf) {
        this.rutaPdf = rutaPdf;
    }

    @Override
    public String toString() {
        return "Factura{" +
                "id=" + id +
                ", codigoFactura='" + codigoFactura + '\'' +
                ", fechaEmision=" + fechaEmision +
                ", total=" + total +
                '}';
    }
}
