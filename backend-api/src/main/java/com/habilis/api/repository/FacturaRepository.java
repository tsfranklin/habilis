package com.habilis.api.repository;

import com.habilis.api.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Factura
 */
@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    /**
     * Buscar factura por código único
     */
    Optional<Factura> findByCodigoFactura(String codigoFactura);

    /**
     * Listar facturas de un usuario ordenadas por fecha (más recientes primero)
     */
    List<Factura> findByUsuarioIdOrderByFechaEmisionDesc(Long usuarioId);

    /**
     * Contar facturas emitidas en un rango de fechas
     * Usado para generar código secuencial
     */
    @Query("SELECT COUNT(f) FROM Factura f WHERE f.fechaEmision BETWEEN :inicio AND :fin")
    long countByFechaEmisionBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Verificar si existe una factura para un pedido
     */
    boolean existsByPedidoId(Long pedidoId);

    /**
     * Buscar factura por ID de pedido
     */
    Optional<Factura> findByPedidoId(Long pedidoId);
}
