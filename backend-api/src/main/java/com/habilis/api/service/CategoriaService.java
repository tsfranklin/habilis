package com.habilis.api.service;

import com.habilis.api.entity.Categoria;
import com.habilis.api.entity.Producto;
import com.habilis.api.repository.CategoriaRepository;
import com.habilis.api.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para gestión de categorías
 */
@Service
@Transactional
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    /**
     * Listar todas las categorías
     */
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    /**
     * Buscar categoría por ID
     */
    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));
    }

    /**
     * Crear una nueva categoría
     */
    public Categoria crear(String nombre, String descripcion) {
        // Verificar que el nombre no esté duplicado
        if (categoriaRepository.findByNombre(nombre).isPresent()) {
            throw new RuntimeException("Ya existe una categoría con ese nombre");
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setDescripcion(descripcion);

        return categoriaRepository.save(categoria);
    }

    /**
     * Actualizar categoría existente
     */
    public Categoria actualizar(Long id, String nombre, String descripcion) {
        Categoria categoria = buscarPorId(id);

        // Verificar que el nombre no esté duplicado (excepto para sí misma)
        categoriaRepository.findByNombre(nombre).ifPresent(existente -> {
            if (!existente.getId().equals(id)) {
                throw new RuntimeException("Ya existe otra categoría con ese nombre");
            }
        });

        categoria.setNombre(nombre);
        categoria.setDescripcion(descripcion);

        return categoriaRepository.save(categoria);
    }

    /**
     * Eliminar categoría
     * NO permite eliminar si tiene productos asociados
     */
    public void eliminar(Long id) {
        Categoria categoria = buscarPorId(id);

        // Verificar si tiene productos asociados
        List<Producto> productos = productoRepository.findByCategoriaId(id);
        if (!productos.isEmpty()) {
            throw new RuntimeException(
                    "No se puede eliminar la categoría porque tiene " +
                            productos.size() + " producto(s) asociado(s). " +
                            "Elimina primero los productos o cámbialos de categoría.");
        }

        categoriaRepository.delete(categoria);
    }

    /**
     * Contar productos por categoría
     */
    public long contarProductos(Long categoriaId) {
        return productoRepository.findByCategoriaId(categoriaId).size();
    }
}
