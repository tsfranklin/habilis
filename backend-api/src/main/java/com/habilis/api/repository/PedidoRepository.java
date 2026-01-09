package com.habilis.api.repository;

import com.habilis.api.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad Pedido
 */
@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /**
     * Busca todos los pedidos de un usuario específico
     * 
     * @param usuarioId ID del usuario
     * @return Lista de pedidos ordenados por fecha descendente
     */
    List<Pedido> findByUsuarioIdOrderByFechaPedidoDesc(Long usuarioId);

    /**
     * Busca pedidos por estado
     * 
     * @param estado Estado del pedido ('PENDIENTE', 'ENVIADO', etc.)
     * @return Lista de pedidos con ese estado
     */
    List<Pedido> findByEstado(String estado);

    /**
     * Obtiene todos los pedidos ordenados por fecha descendente
     * 
     * @return Lista de todos los pedidos
     */
    @Query("SELECT p FROM Pedido p ORDER BY p.fechaPedido DESC")
    List<Pedido> findAllOrderByFechaDesc();
}
