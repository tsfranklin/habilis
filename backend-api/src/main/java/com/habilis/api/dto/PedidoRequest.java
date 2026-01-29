package com.habilis.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * DTO para crear un nuevo pedido
 */
public class PedidoRequest {

    // El usuarioId se obtiene de la sesión HTTP en el controlador,
    // no es necesario enviarlo en el request
    private Long usuarioId;

    @NotEmpty(message = "El pedido debe tener al menos un producto")
    @Valid
    private List<ItemPedidoRequest> items;

    // Constructores
    public PedidoRequest() {
    }

    public PedidoRequest(Long usuarioId, List<ItemPedidoRequest> items) {
        this.usuarioId = usuarioId;
        this.items = items;
    }

    // Getters y Setters
    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public List<ItemPedidoRequest> getItems() {
        return items;
    }

    public void setItems(List<ItemPedidoRequest> items) {
        this.items = items;
    }
}
