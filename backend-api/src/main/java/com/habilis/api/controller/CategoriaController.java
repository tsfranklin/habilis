package com.habilis.api.controller;

import com.habilis.api.dto.CategoriaRequest;
import com.habilis.api.entity.Categoria;
import com.habilis.api.service.CategoriaService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de categorías
 * La mayoría de operaciones requieren rol ADMIN
 */
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    /**
     * GET /api/categorias
     * Listar todas las categorías (público)
     */
    @GetMapping
    public ResponseEntity<List<Categoria>> listarTodas() {
        List<Categoria> categorias = categoriaService.listarTodas();
        return ResponseEntity.ok(categorias);
    }

    /**
     * GET /api/categorias/{id}
     * Obtener una categoría por ID (público)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            Categoria categoria = categoriaService.buscarPorId(id);
            return ResponseEntity.ok(categoria);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/categorias
     * Crear nueva categoría (solo ADMIN)
     */
    @PostMapping
    public ResponseEntity<?> crear(
            @Valid @RequestBody CategoriaRequest request,
            HttpSession session) {

        // Verificar que haya sesión activa
        String tipoUsuario = (String) session.getAttribute("userRole");
        if (tipoUsuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "Debes iniciar sesión primero"));
        }

        // Verificar que sea ADMIN
        if (!"ADMIN".equals(tipoUsuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Solo los administradores pueden crear categorías"));
        }

        try {
            Categoria categoria = categoriaService.crear(
                    request.getNombre(),
                    request.getDescripcion());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Categoría creada exitosamente");
            response.put("categoria", categoria);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/categorias/{id}
     * Actualizar categoría existente (solo ADMIN)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequest request,
            HttpSession session) {

        // Verificar rol ADMIN
        String tipoUsuario = (String) session.getAttribute("userRole");
        if (tipoUsuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "Debes iniciar sesión primero"));
        }

        if (!"ADMIN".equals(tipoUsuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Solo los administradores pueden modificar categorías"));
        }

        try {
            Categoria categoria = categoriaService.actualizar(
                    id,
                    request.getNombre(),
                    request.getDescripcion());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Categoría actualizada exitosamente");
            response.put("categoria", categoria);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/categorias/{id}
     * Eliminar categoría (solo ADMIN)
     * NO permite eliminar si tiene productos asociados
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
                    Map.of("error", "Solo los administradores pueden eliminar categorías"));
        }

        try {
            categoriaService.eliminar(id);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Categoría eliminada exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/categorias/{id}/productos/count
     * Contar productos de una categoría (público)
     */
    @GetMapping("/{id}/productos/count")
    public ResponseEntity<?> contarProductos(@PathVariable Long id) {
        try {
            long count = categoriaService.contarProductos(id);
            return ResponseEntity.ok(Map.of(
                    "categoriaId", id,
                    "cantidadProductos", count));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage()));
        }
    }
}
