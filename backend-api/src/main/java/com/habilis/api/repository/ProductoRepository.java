package com.habilis.api.repository;

import com.habilis.api.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repositorio para la entidad Producto
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

        /**
         * Busca productos por categoría
         * 
         * @param categoriaId ID de la categoría
         * @return Lista de productos de esa categoría
         */
        List<Producto> findByCategoriaId(Long categoriaId);

        /**
         * Busca productos cuyo nombre contenga el texto dado (búsqueda parcial)
         * 
         * @param nombre Texto a buscar
         * @return Lista de productos que coinciden
         */
        List<Producto> findByNombreContainingIgnoreCase(String nombre);

        /**
         * Listar productos con stock menor a un valor dado
         */
        List<Producto> findByStockLessThan(int umbral);

        /**
         * Busca productos con stock mayor a 0
         * 
         * @return Lista de productos disponibles
         */
        @Query("SELECT p FROM Producto p WHERE p.stock > 0")
        List<Producto> findProductosDisponibles();

        /**
         * Búsqueda avanzada con múltiples criterios (todos opcionales)
         * 
         * @param nombre      Texto a buscar en el nombre (puede ser null)
         * @param categoriaId ID de la categoría (puede ser null)
         * @param precioMin   Precio mínimo (puede ser null)
         * @param precioMax   Precio máximo (puede ser null)
         * @param disponible  Solo productos con stock > 0 (puede ser null)
         * @return Lista de productos que coinciden con los criterios
         */
        @Query("SELECT p FROM Producto p WHERE " +
                        "(:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
                        "(:categoriaId IS NULL OR p.categoria.id = :categoriaId) AND " +
                        "(:precioMin IS NULL OR p.precio >= :precioMin) AND " +
                        "(:precioMax IS NULL OR p.precio <= :precioMax) AND " +
                        "(:disponible IS NULL OR (:disponible = true AND p.stock > 0) OR (:disponible = false))")
        List<Producto> buscarConFiltros(
                        @Param("nombre") String nombre,
                        @Param("categoriaId") Long categoriaId,
                        @Param("precioMin") BigDecimal precioMin,
                        @Param("precioMax") BigDecimal precioMax,
                        @Param("disponible") Boolean disponible);
}
