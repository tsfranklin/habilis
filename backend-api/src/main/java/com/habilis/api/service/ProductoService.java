package com.habilis.api.service;

import com.habilis.api.entity.Categoria;
import com.habilis.api.entity.Producto;
import com.habilis.api.repository.CategoriaRepository;
import com.habilis.api.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio para gestión de productos
 */
@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    /**
     * Listar todos los productos
     */
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    /**
     * Buscar producto por ID
     */
    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
    }

    /**
     * Listar productos por categoría
     */
    public List<Producto> listarPorCategoria(Long categoriaId) {
        return productoRepository.findByCategoriaId(categoriaId);
    }

    /**
     * Buscar productos por nombre (búsqueda parcial)
     */
    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    /**
     * Listar productos con stock bajo (menos de X unidades)
     */
    public List<Producto> listarConStockBajo(int umbral) {
        return productoRepository.findByStockLessThan(umbral);
    }

    /**
     * Crear un nuevo producto
     */
    public Producto crear(Long categoriaId, String nombre, String descripcion,
            BigDecimal precio, Integer stock, String imagenUrl) {
        // Verificar que la categoría existe
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + categoriaId));

        // Crear producto
        Producto producto = new Producto();
        producto.setCategoria(categoria);
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setStock(stock);
        producto.setImagenUrl(imagenUrl);

        return productoRepository.save(producto);
    }

    /**
     * Actualizar producto existente
     */
    public Producto actualizar(Long id, Long categoriaId, String nombre, String descripcion,
            BigDecimal precio, Integer stock, String imagenUrl) {
        Producto producto = buscarPorId(id);

        // Si se cambió la categoría, verificar que existe
        if (!producto.getCategoria().getId().equals(categoriaId)) {
            Categoria nuevaCategoria = categoriaRepository.findById(categoriaId)
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + categoriaId));
            producto.setCategoria(nuevaCategoria);
        }

        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setStock(stock);
        producto.setImagenUrl(imagenUrl);

        return productoRepository.save(producto);
    }

    /**
     * Actualizar solo el stock de un producto
     */
    public Producto actualizarStock(Long id, Integer nuevoStock) {
        Producto producto = buscarPorId(id);

        if (nuevoStock < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }

        producto.setStock(nuevoStock);
        return productoRepository.save(producto);
    }

    /**
     * Reducir stock (útil para pedidos)
     */
    public void reducirStock(Long id, Integer cantidad) {
        Producto producto = buscarPorId(id);

        if (producto.getStock() < cantidad) {
            throw new RuntimeException(
                    "Stock insuficiente. Disponible: " + producto.getStock() +
                            ", solicitado: " + cantidad);
        }

        producto.setStock(producto.getStock() - cantidad);
        productoRepository.save(producto);
    }

    /**
     * Aumentar stock (útil para devoluciones o restock)
     */
    public void aumentarStock(Long id, Integer cantidad) {
        Producto producto = buscarPorId(id);
        producto.setStock(producto.getStock() + cantidad);
        productoRepository.save(producto);
    }

    /**
     * Eliminar producto
     * NOTA: En producción, considerar "soft delete" en lugar de eliminar
     */
    public void eliminar(Long id) {
        Producto producto = buscarPorId(id);

        // TODO: Verificar que no tenga pedidos asociados
        // Por ahora permitimos la eliminación

        productoRepository.delete(producto);
    }

    /**
     * Verificar si hay stock disponible
     */
    public boolean hayStockDisponible(Long id, Integer cantidadRequerida) {
        Producto producto = buscarPorId(id);
        return producto.getStock() >= cantidadRequerida;
    }
}
