package com.habilis.api.controller;

import com.habilis.api.dto.ProductoRequest;
import com.habilis.api.entity.Producto;
import com.habilis.api.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de productos
 * La mayoría de operaciones requieren rol ADMIN
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * GET /api/productos
     * Listar todos los productos (público)
     */
    @GetMapping
    public ResponseEntity<List<Producto>> listarTodos() {
        List<Producto> productos = productoService.listarTodos();
        return ResponseEntity.ok(productos);
    }

    /**
     * GET /api/productos/{id}
     * Obtener un producto por ID (público)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            Producto producto = productoService.buscarPorId(id);
            return ResponseEntity.ok(producto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/productos/categoria/{categoriaId}
     * Listar productos por categoría (público)
     */
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Producto>> listarPorCategoria(@PathVariable Long categoriaId) {
        List<Producto> productos = productoService.listarPorCategoria(categoriaId);
        return ResponseEntity.ok(productos);
    }

    /**
     * GET /api/productos/buscar?nombre=xxx
     * Buscar productos por nombre (público)
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<Producto>> buscarPorNombre(@RequestParam String nombre) {
        List<Producto> productos = productoService.buscarPorNombre(nombre);
        return ResponseEntity.ok(productos);
    }

    /**
     * GET /api/productos/stock-bajo?umbral=10
     * Listar productos con stock bajo (solo ADMIN)
     */
    @GetMapping("/stock-bajo")
    public ResponseEntity<?> listarConStockBajo(
            @RequestParam(defaultValue = "10") int umbral,
            HttpSession session) {

        String tipoUsuario = (String) session.getAttribute("userRole");
        if (tipoUsuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "Debes iniciar sesión primero"));
        }

        if (!"ADMIN".equals(tipoUsuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Solo los administradores pueden ver esta información"));
        }

        List<Producto> productos = productoService.listarConStockBajo(umbral);
        return ResponseEntity.ok(productos);
    }

    /**
     * POST /api/productos
     * Crear nuevo producto (solo ADMIN)
     */
    @PostMapping
    public ResponseEntity<?> crear(
            @Valid @RequestBody ProductoRequest request,
            HttpSession session) {

        // Verificar rol ADMIN
        String tipoUsuario = (String) session.getAttribute("userRole");
        if (tipoUsuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "Debes iniciar sesión primero"));
        }

        if (!"ADMIN".equals(tipoUsuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Solo los administradores pueden crear productos"));
        }

        try {
            Producto producto = productoService.crear(
                    request.getCategoriaId(),
                    request.getNombre(),
                    request.getDescripcion(),
                    request.getPrecio(),
                    request.getStock(),
                    request.getImagenUrl());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Producto creado exitosamente");
            response.put("producto", producto);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/productos/{id}
     * Actualizar producto existente (solo ADMIN)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest request,
            HttpSession session) {

        // Verificar rol ADMIN
        String tipoUsuario = (String) session.getAttribute("userRole");
        if (tipoUsuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "Debes iniciar sesión primero"));
        }

        if (!"ADMIN".equals(tipoUsuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Solo los administradores pueden modificar productos"));
        }

        try {
            Producto producto = productoService.actualizar(
                    id,
                    request.getCategoriaId(),
                    request.getNombre(),
                    request.getDescripcion(),
                    request.getPrecio(),
                    request.getStock(),
                    request.getImagenUrl());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Producto actualizado exitosamente");
            response.put("producto", producto);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage()));
        }
    }

    /**
     * PATCH /api/productos/{id}/stock
     * Actualizar solo el stock de un producto (solo ADMIN)
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<?> actualizarStock(
            @PathVariable Long id,
            @RequestParam Integer nuevoStock,
            HttpSession session) {

        // Verificar rol ADMIN
        String tipoUsuario = (String) session.getAttribute("userRole");
        if (tipoUsuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "Debes iniciar sesión primero"));
        }

        if (!"ADMIN".equals(tipoUsuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Solo los administradores pueden modificar el stock"));
        }

        try {
            Producto producto = productoService.actualizarStock(id, nuevoStock);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Stock actualizado exitosamente");
            response.put("producto", producto);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/productos/{id}
     * Eliminar producto (solo ADMIN)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(
            @PathVariable Long id,
            HttpSession session) {

        // Verificar rol ADMIN
        String tipoUsuario = (String) session.getAttribute("userRole");
        if (tipoUsuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "Debes iniciar sesión primero"));
        }

        if (!"ADMIN".equals(tipoUsuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Solo los administradores pueden eliminar productos"));
        }

        try {
            productoService.eliminar(id);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Producto eliminado exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/productos/{id}/stock-disponible?cantidad=5
     * Verificar si hay stock disponible (público)
     */
    @GetMapping("/{id}/stock-disponible")
    public ResponseEntity<?> verificarStock(
            @PathVariable Long id,
            @RequestParam Integer cantidad) {
        try {
            boolean disponible = productoService.hayStockDisponible(id, cantidad);

            return ResponseEntity.ok(Map.of(
                    "productoId", id,
                    "cantidadSolicitada", cantidad,
                    "disponible", disponible));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage()));
        }
    }
}
