package com.habilis.api.repository;

import com.habilis.api.entity.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad DetallePedido
 */
@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    /**
     * Busca todos los detalles de un pedido específico
     * 
     * @param pedidoId ID del pedido
     * @return Lista de detalles del pedido
     */
    List<DetallePedido> findByPedidoId(Long pedidoId);

    /**
     * Busca todos los pedidos que incluyen un producto específico
     * 
     * @param productoId ID del producto
     * @return Lista de detalles que contienen ese producto
     */
    List<DetallePedido> findByProductoId(Long productoId);
}
