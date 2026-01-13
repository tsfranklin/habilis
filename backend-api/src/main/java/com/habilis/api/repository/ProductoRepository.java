package com.habilis.api.repository;

import com.habilis.api.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
         * Búsqueda avanzada por nombre y/o categoría
         * 
         * @param nombre      Texto a buscar en el nombre
         * @param categoriaId ID de la categoría (puede ser null)
         * @return Lista de productos que coinciden
         */
        @Query("SELECT p FROM Producto p WHERE " +
                        "(:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
                        "(:categoriaId IS NULL OR p.categoria.id = :categoriaId)")
        List<Producto> buscarProductos(@Param("nombre") String nombre,
                        @Param("categoriaId") Long categoriaId);
}
